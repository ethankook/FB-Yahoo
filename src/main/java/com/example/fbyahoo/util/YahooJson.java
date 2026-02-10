package com.example.fbyahoo.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class YahooJson {

    private YahooJson() {}

    public static String text(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    public static Integer intOrNull(JsonNode node, String field) {
        String s = text(node, field);
        if (s == null || s.isBlank() || s.equals("-")) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }

    public static Long longOrNull(JsonNode node, String field) {
        String s = text(node, field);
        if (s == null || s.isBlank() || s.equals("-")) return null;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    public static Boolean boolOrNull(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;

        if (v.isBoolean()) return v.asBoolean();
        String s = v.asText();
        if ("1".equals(s) || "true".equalsIgnoreCase(s)) return true;
        if ("0".equals(s) || "false".equalsIgnoreCase(s)) return false;
        return null;
    }

    public static BigDecimal bigDecimalOrNull(String s) {
        if (s == null || s.isBlank() || s.equals("-")) return null;
        try { return new BigDecimal(s); } catch (NumberFormatException e) { return null; }
    }

    public static LocalDate dateOrNull(String s) {
        if (s == null || s.isBlank() || s.equals("-")) return null;
        return LocalDate.parse(s);
    }

    public static LocalDate dateFieldOrNull(JsonNode node, String field) {
        return dateOrNull(text(node, field));
    }
}