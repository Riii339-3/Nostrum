package io.github.riiimc.nostrum.content.upgrade.custom

import io.github.riiimc.nostrum.NostrumRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.damagesource.DamageSource
import net.neoforged.bus.api.Event
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.Random


object AlchemicalExpression {
    private val random = Random()

    private fun invoke(
        target: Any,
        methodName: String,
        args: List<Any?>
    ): Any? {
        val clazz =
            target as? Class<*> ?: target.javaClass

        val method = clazz.methods
            .firstOrNull { method ->
                method.name == methodName &&
                        method.parameterCount == args.size &&
                        method.parameterTypes
                            .zip(args)
                            .all { (type, value) ->
                                value == null ||
                                        convertArgument(value, type) != null
                            }
            }
            ?: error(
                "No matching method '$methodName' on ${clazz.name}"
            )

        val convertedArgs = args
            .zip(method.parameterTypes)
            .map { (value, type) ->
                convertArgument(value, type)
            }
            .toTypedArray()

        return method.invoke(
            if (Modifier.isStatic(method.modifiers)) null else target,
            *convertedArgs
        )
    }

    private fun convertArgument(
        value: Any?,
        targetType: Class<*>
    ): Any? {
        if (value == null) {
            if (targetType.isPrimitive) {
                error(
                    "Cannot pass null to primitive ${targetType.name}"
                )
            }

            return null
        }

        if (targetType.isAssignableFrom(value.javaClass)) {
            return value
        }

        if (value is Number) {
            return when (targetType) {
                java.lang.Byte.TYPE,
                Byte::class.java ->
                    value.toByte()

                java.lang.Short.TYPE,
                Short::class.java ->
                    value.toShort()

                Integer.TYPE,
                Int::class.java ->
                    value.toInt()

                java.lang.Long.TYPE,
                Long::class.java ->
                    value.toLong()

                java.lang.Float.TYPE,
                Float::class.java ->
                    value.toFloat()

                java.lang.Double.TYPE,
                Double::class.java ->
                    value.toDouble()

                else -> null
            }
        }

        return null
    }

    private fun findAssignment(
        expression: String
    ): Assignment? {
        var depth = 0

        for (i in expression.indices) {
            when (expression[i]) {
                '(' -> depth++
                ')' -> depth--

                '=' -> {
                    if (depth != 0) continue

                    // == は比較演算子なので除外
                    if (
                        (i > 0 && expression[i - 1] == '=') ||
                        (i + 1 < expression.length && expression[i + 1] == '=')
                    ) {
                        continue
                    }

                    val left = expression
                        .substring(0, i)
                        .trim()

                    val right = expression
                        .substring(i + 1)
                        .trim()

                    val dot = left.lastIndexOf('.')

                    require(dot != -1) {
                        "Invalid assignment: $expression"
                    }

                    return Assignment(
                        target = left.substring(0, dot),
                        property = left.substring(dot + 1),
                        value = right
                    )
                }
            }
        }

        return null
    }

    private fun findField(
        clazz: Class<*>,
        name: String
    ): Field {
        return clazz.getField(name)
            .apply {
                isAccessible = true
            }
    }

    private fun readField(
        target: Any,
        name: String
    ): Any? {
        val clazz =
            target as? Class<*> ?: target.javaClass

        val field = findField(clazz, name)

        return field.get(
            if (Modifier.isStatic(field.modifiers)) {
                null
            } else {
                target
            }
        )
    }

    fun execute(
        expression: String,
        variables: Map<String, Any?>
    ): Any? {
        val value = expression.trim()

        // 代入
        findAssignment(value)?.let { assignment ->
            val target = evaluateArgument(
                assignment.target,
                variables
            )

            requireNotNull(target) {
                "Cannot assign ${assignment.property} on null"
            }

            val field = findField(
                target.javaClass,
                assignment.property
            )

            val rawValue = evaluateArgument(
                assignment.value,
                variables
            )

            val converted = convertArgument(
                rawValue,
                field.type
            )

            require(
                converted != null || !field.type.isPrimitive
            ) {
                "Cannot assign $rawValue to ${field.type.name}"
            }

            field.set(target, converted)

            return converted
        }

        val chain = parseChain(value)

        var current =
            resolveVariable(chain.first().name, variables)

        for (call in chain.drop(1)) {
            val target = requireNotNull(current) {
                "Cannot access ${call.name} on null"
            }

            current =
                if (call.arguments.isEmpty()) {
                    try {
                        readField(target, call.name)
                    } catch (_: NoSuchFieldException) {
                        invoke(
                            target,
                            call.name,
                            emptyList()
                        )
                    }
                } else {
                    invoke(
                        target,
                        call.name,
                        call.arguments.map {
                            evaluateArgument(it, variables)
                        }
                    )
                }
        }

        return current
    }
    fun evaluateCondition(
        expression: String,
        variables: Map<String, Any?>
    ): Boolean {
        val value = expression.trim()

        // OR
        splitTopLevel(value, "||").let { parts ->
            if (parts.size > 1) {
                return parts.any {
                    evaluateCondition(it, variables)
                }
            }
        }

        // AND
        splitTopLevel(value, "&&").let { parts ->
            if (parts.size > 1) {
                return parts.all {
                    evaluateCondition(it, variables)
                }
            }
        }

        // NOT
        if (value.startsWith("!")) {
            return !evaluateCondition(
                value.substring(1).trim(),
                variables
            )
        }

        // 括弧
        if (value.startsWith("(") && value.endsWith(")")) {
            val inner = value.substring(1, value.length - 1)

            if (isWrappedByParentheses(inner)) {
                return evaluateCondition(
                    inner,
                    variables
                )
            }
        }

        // 比較
        findComparison(value)?.let { comparison ->
            val left = evaluateArgument(
                comparison.left,
                variables
            )

            val right = evaluateArgument(
                comparison.right,
                variables
            )

            return when (comparison.operator) {
                "==" -> left == right
                "!=" -> left != right

                ">" -> compareValues(left, right) > 0
                ">=" -> compareValues(left, right) >= 0
                "<" -> compareValues(left, right) < 0
                "<=" -> compareValues(left, right) <= 0

                else -> error(
                    "Unknown comparison operator: ${comparison.operator}"
                )
            }
        }

        // 比較演算子がない場合は Boolean 式として評価
        return evaluateArgument(
            value,
            variables
        ) as? Boolean
            ?: error(
                "Condition did not evaluate to Boolean: $expression"
            )
    }

    private data class Comparison(
        val left: String,
        val operator: String,
        val right: String
    )

    private fun findComparison(
        expression: String
    ): Comparison? {
        var depth = 0

        val operators = listOf(
            "==",
            "!=",
            ">=",
            "<=",
            ">",
            "<"
        )

        var i = 0

        while (i < expression.length) {
            when (expression[i]) {
                '(' -> depth++
                ')' -> depth--
            }

            if (depth == 0) {
                for (operator in operators) {
                    if (expression.startsWith(operator, i)) {
                        return Comparison(
                            left = expression
                                .substring(0, i)
                                .trim(),

                            operator = operator,

                            right = expression
                                .substring(i + operator.length)
                                .trim()
                        )
                    }
                }
            }

            i++
        }

        return null
    }



    private fun splitTopLevel(
        expression: String,
        separator: String
    ): List<String> {
        val result = mutableListOf<String>()

        var depth = 0
        var start = 0
        var i = 0

        while (i < expression.length) {
            when (expression[i]) {
                '(' -> depth++
                ')' -> depth--
            }

            if (
                depth == 0 &&
                expression.startsWith(separator, i)
            ) {
                result += expression
                    .substring(start, i)
                    .trim()

                i += separator.length
                start = i
                continue
            }

            i++
        }

        result += expression
            .substring(start)
            .trim()

        return result
    }

    private fun isWrappedByParentheses(
        expression: String
    ): Boolean {
        var depth = 0

        for (i in expression.indices) {
            when (expression[i]) {
                '(' -> depth++
                ')' -> {
                    depth--

                    if (depth == 0 && i != expression.lastIndex) {
                        return false
                    }
                }
            }
        }

        return depth == 0
    }

    private fun compareValues(
        left: Any?,
        right: Any?
    ): Int {
        requireNotNull(left) {
            "Cannot compare null"
        }

        requireNotNull(right) {
            "Cannot compare null"
        }

        if (left is Number && right is Number) {
            return left.toDouble()
                .compareTo(right.toDouble())
        }

        if (
            left is Comparable<*> &&
            left.javaClass.isAssignableFrom(right.javaClass)
        ) {
            @Suppress("UNCHECKED_CAST")
            return (left as Comparable<Any>)
                .compareTo(right)
        }

        error(
            "Cannot compare ${left.javaClass.name} " +
                    "and ${right.javaClass.name}"
        )
    }

    private data class Call(
        val name: String,
        val arguments: List<String>
    )

    private fun parseChain(
        expression: String
    ): List<Call> {
        val result = mutableListOf<Call>()

        var depth = 0
        var start = 0

        for (i in expression.indices) {
            when (expression[i]) {
                '(' -> depth++

                ')' -> depth--

                '.' -> {
                    if (depth != 0) continue

                    // 数字.数字 は小数点
                    if (
                        i > 0 &&
                        i + 1 < expression.length &&
                        expression[i - 1].isDigit() &&
                        expression[i + 1].isDigit()
                    ) {
                        continue
                    }

                    result += parseCall(
                        expression.substring(start, i)
                    )

                    start = i + 1
                }
            }
        }

        result += parseCall(
            expression.substring(start)
        )

        return result
    }
    private fun parseCall(
        value: String
    ): Call {
        val open = value.indexOf('(')

        if (open == -1) {
            return Call(
                name = value.trim(),
                arguments = emptyList()
            )
        }

        require(value.endsWith(")")) {
            "Invalid expression: $value"
        }

        val name = value
            .substring(0, open)
            .trim()

        val arguments = splitArguments(
            value.substring(
                open + 1,
                value.length - 1
            )
        )

        return Call(
            name,
            arguments
        )
    }

    private fun calculate(
        left: Any?,
        operator: String,
        right: Any?
    ): Any {
        require(left is Number) {
            "Left operand must be Number: $left"
        }

        require(right is Number) {
            "Right operand must be Number: $right"
        }

        val l = left.toDouble()
        val r = right.toDouble()

        return when (operator) {
            "+" -> l + r
            "-" -> l - r
            "*" -> l * r
            "/" -> l / r
            "%" -> l % r

            else -> error(
                "Unknown arithmetic operator: $operator"
            )
        }
    }

    private fun splitArguments(
        value: String
    ): List<String> {
        if (value.isBlank()) {
            return emptyList()
        }

        val result = mutableListOf<String>()

        var depth = 0
        var start = 0

        for (i in value.indices) {
            when (value[i]) {
                '(' -> depth++
                ')' -> depth--

                ',' -> {
                    if (depth == 0) {
                        result += value
                            .substring(start, i)
                            .trim()

                        start = i + 1
                    }
                }
            }
        }

        result += value
            .substring(start)
            .trim()

        return result
    }

    private fun findArithmetic(
        expression: String
    ): Arithmetic? {
        var depth: Int

        // + - より先に * / %
        val operators = listOf(
            "+",
            "-",
            "*",
            "/",
            "%"
        )

        for (priority in 0..1) {
            val candidates =
                if (priority == 0) {
                    listOf("+", "-")
                } else {
                    listOf("*", "/", "%")
                }

            depth = 0

            for (i in expression.indices.reversed()) {
                when (expression[i]) {
                    ')' -> depth++
                    '(' -> depth--
                }

                if (depth != 0) continue

                for (operator in candidates) {
                    if (
                        expression.startsWith(
                            operator,
                            i
                        )
                    ) {
                        // 単項マイナスは除外
                        if (
                            operator == "-" &&
                            (
                                    i == 0 ||
                                            expression[i - 1] in "+-*/%"
                                    )
                        ) {
                            continue
                        }

                        return Arithmetic(
                            left = expression
                                .substring(0, i)
                                .trim(),

                            operator = operator,

                            right = expression
                                .substring(i + 1)
                                .trim()
                        )
                    }
                }
            }
        }

        return null
    }

    private fun evaluateArgument(
        expression: String,
        variables: Map<String, Any?>
    ): Any? {
        val value = expression.trim()

        // 算術演算
        findArithmetic(value)?.let { arithmetic ->
            val left = evaluateArgument(
                arithmetic.left,
                variables
            )

            val right = evaluateArgument(
                arithmetic.right,
                variables
            )

            return calculate(
                left,
                arithmetic.operator,
                right
            )
        }

        // 数値
        value.toIntOrNull()?.let {
            return it
        }

        value.toDoubleOrNull()?.let {
            return it
        }

        // Boolean
        when (value) {
            "true" -> return true
            "false" -> return false
            "null" -> return null
        }

        // String
        if (
            value.length >= 2 &&
            value.first() == '"' &&
            value.last() == '"'
        ) {
            return value.substring(
                1,
                value.length - 1
            )
        }

        // 式
        return execute(
            value,
            variables
        )
    }
    private fun resolveVariable(
        name: String,
        variables: Map<String, Any?>
    ): Any? {
        if (!variables.containsKey(name)) {
            error("Unknown variable: $name")
        }

        return variables[name]
    }

    fun createVariables(
        event: Event
    ): MutableMap<String, Any?> {
        return mutableMapOf(
            "event" to event,

            "ALCHEMICAL_UPGRADE_COMPONENT" to
                    NostrumRegistries.ALCHEMICAL_UPGRADE_COMPONENT,

            "ResourceLocation" to
                    ResourceLocation::class.java,

            "Random" to random,

            "ResourceKey" to
                    ResourceKey::class.java,
            "Registries" to
                    Registries::class.java,
            "DamageSource" to DamageSource::class.java
        )
    }

    private data class Arithmetic(
        val left: String,
        val operator: String,
        val right: String
    )

    private data class Assignment(
        val target: String,
        val property: String,
        val value: String
    )
}