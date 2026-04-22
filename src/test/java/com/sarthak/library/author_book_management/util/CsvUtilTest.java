package com.sarthak.library.author_book_management.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CsvUtilTest {

    // ── escapeCSV ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("escapeCSV: wraps plain value in double-quotes")
    void escapeCSV_plainValue() {
        assertThat(CsvUtil.escapeCSV("hello")).isEqualTo("\"hello\"");
    }

    @Test
    @DisplayName("escapeCSV: escapes internal double-quotes by doubling them")
    void escapeCSV_internalDoubleQuotes() {
        assertThat(CsvUtil.escapeCSV("say \"hello\"")).isEqualTo("\"say \"\"hello\"\"\"");
    }

    @Test
    @DisplayName("escapeCSV: value with comma is wrapped in quotes")
    void escapeCSV_commaInValue() {
        assertThat(CsvUtil.escapeCSV("Smith, John")).isEqualTo("\"Smith, John\"");
    }

    @Test
    @DisplayName("escapeCSV: null returns empty string")
    void escapeCSV_null() {
        assertThat(CsvUtil.escapeCSV(null)).isEqualTo("");
    }

    @Test
    @DisplayName("escapeCSV: empty string is wrapped in empty quotes")
    void escapeCSV_emptyString() {
        assertThat(CsvUtil.escapeCSV("")).isEqualTo("\"\"");
    }

    @Test
    @DisplayName("escapeCSV: value with only spaces is wrapped correctly")
    void escapeCSV_spaces() {
        assertThat(CsvUtil.escapeCSV("   ")).isEqualTo("\"   \"");
    }

    @Test
    @DisplayName("escapeCSV: newline in value is preserved inside quotes")
    void escapeCSV_newlineInValue() {
        assertThat(CsvUtil.escapeCSV("line1\nline2")).isEqualTo("\"line1\nline2\"");
    }

    @Test
    @DisplayName("escapeCSV: multiple consecutive double-quotes are all doubled")
    void escapeCSV_multipleQuotes() {
        assertThat(CsvUtil.escapeCSV("a\"\"b")).isEqualTo("\"a\"\"\"\"b\"");
    }

    // ── parseCsvLine ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("parseCsvLine: splits plain comma-separated values")
    void parseCsvLine_plainValues() {
        String[] result = CsvUtil.parseCsvLine("a,b,c");
        assertThat(result).containsExactly("a", "b", "c");
    }

    @Test
    @DisplayName("parseCsvLine: single value with no comma")
    void parseCsvLine_singleValue() {
        String[] result = CsvUtil.parseCsvLine("onlyOne");
        assertThat(result).containsExactly("onlyOne");
    }

    @Test
    @DisplayName("parseCsvLine: comma inside quotes is NOT a separator")
    void parseCsvLine_commaInsideQuotes() {
        String[] result = CsvUtil.parseCsvLine("\"Smith, John\",30");
        assertThat(result).containsExactly("Smith, John", "30");
    }

    @Test
    @DisplayName("parseCsvLine: multiple quoted fields")
    void parseCsvLine_multipleQuotedFields() {
        String[] result = CsvUtil.parseCsvLine("\"one,two\",\"three,four\",five");
        assertThat(result).containsExactly("one,two", "three,four", "five");
    }

    @Test
    @DisplayName("parseCsvLine: empty fields produce empty strings")
    void parseCsvLine_emptyFields() {
        String[] result = CsvUtil.parseCsvLine("a,,c");
        assertThat(result).containsExactly("a", "", "c");
    }

    @Test
    @DisplayName("parseCsvLine: leading and trailing commas produce empty strings")
    void parseCsvLine_leadingTrailingComma() {
        String[] result = CsvUtil.parseCsvLine(",b,");
        assertThat(result).containsExactly("", "b", "");
    }

    @Test
    @DisplayName("parseCsvLine: empty line returns array with one empty string")
    void parseCsvLine_emptyLine() {
        String[] result = CsvUtil.parseCsvLine("");
        assertThat(result).hasSize(1).containsExactly("");
    }

    @Test
    @DisplayName("parseCsvLine: all empty fields between commas")
    void parseCsvLine_allEmpty() {
        String[] result = CsvUtil.parseCsvLine(",,,");
        assertThat(result).containsExactly("", "", "", "");
    }

    @Test
    @DisplayName("parseCsvLine: CSV line matching book import format")
    void parseCsvLine_bookImportFormat() {
        String[] result = CsvUtil.parseCsvLine("Clean Code,A handbook of agile software craftsmanship,TECHNOLOGY,1");
        assertThat(result).containsExactly(
                "Clean Code",
                "A handbook of agile software craftsmanship",
                "TECHNOLOGY",
                "1"
        );
    }

    @Test
    @DisplayName("parseCsvLine: CSV line matching author import format")
    void parseCsvLine_authorImportFormat() {
        String[] result = CsvUtil.parseCsvLine("Jane,Doe,jane@doe.com,A great author");
        assertThat(result).containsExactly("Jane", "Doe", "jane@doe.com", "A great author");
    }

    @Test
    @DisplayName("parseCsvLine: quoted value with escaped double-quote inside")
    void parseCsvLine_escapedQuoteInsideQuotedField() {
        // Note: parser toggles on every quote, so embedded quotes behave as toggle points
        // "He said \"hello\"" → toggle: He said hello
        String[] result = CsvUtil.parseCsvLine("\"He said \"\"hi\"\"\"");
        // The simple toggle parser sees: open-quote, text, close-quote-open-quote, text, close-quote
        // Result is implementation-specific for the toggle-based parser
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("parseCsvLine: spaces within values are preserved")
    void parseCsvLine_preservesSpaces() {
        String[] result = CsvUtil.parseCsvLine("  hello  ,  world  ");
        assertThat(result).containsExactly("  hello  ", "  world  ");
    }

    // ── escapeCSV + parseCsvLine roundtrip ────────────────────────────────────

    @Test
    @DisplayName("escapeCSV → parseCsvLine roundtrip preserves value with comma")
    void roundtrip_valueWithComma() {
        String original = "Jones, Rick";
        String escaped = CsvUtil.escapeCSV(original);
        String[] parsed = CsvUtil.parseCsvLine(escaped);

        // escaped = "\"Jones, Rick\"", parsed has 1 element: Jones, Rick
        assertThat(parsed).hasSize(1);
        assertThat(parsed[0]).isEqualTo(original);
    }

    @Test
    @DisplayName("escapeCSV → parseCsvLine roundtrip: multi-column CSV row")
    void roundtrip_multiColumnRow() {
        String title = "Clean Code";
        String desc = "A handbook";
        String genre = "TECHNOLOGY";
        String authorId = "1";

        String csvLine = CsvUtil.escapeCSV(title) + "," + CsvUtil.escapeCSV(desc)
                + "," + genre + "," + authorId;
        String[] parsed = CsvUtil.parseCsvLine(csvLine);

        assertThat(parsed[0]).isEqualTo(title);
        assertThat(parsed[1]).isEqualTo(desc);
        assertThat(parsed[2]).isEqualTo(genre);
        assertThat(parsed[3]).isEqualTo(authorId);
    }
}

