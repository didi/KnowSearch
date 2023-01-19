/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher.notification.email;

import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.watcher.Watcher;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;


public class HtmlSanitizerTests extends ESTestCase {
    public void testDefaultWithTemplatePlaceholders() {
        String blockTag = randomFrom(HtmlSanitizer.BLOCK_TAGS);
        while (blockTag.equals("li")) {
            blockTag = randomFrom(HtmlSanitizer.BLOCK_TAGS);
        }
        String html =
                "<html>" +
                        "<head></head>" +
                        "<body>" +
                        "<" + blockTag + ">Hello {{ctx.metadata.name}}</" + blockTag + ">" +
                        "<ul><li>item1</li></ul>" +
                        "<ol><li>item2</li></ol>" +
                        "meta <a href='https://www.google.com/search?q={{ctx.metadata.name}}'>Testlink</a> meta" +
                        "</body>" +
                        "</html>";
        HtmlSanitizer sanitizer = new HtmlSanitizer(Settings.EMPTY);
        String sanitizedHtml = sanitizer.sanitize(html);
        if (blockTag.equals("ol") || blockTag.equals("ul")) {
            assertThat(sanitizedHtml, equalTo(
                    "<head></head><body>" +
                            "<" + blockTag + "><li>Hello {{ctx.metadata.name}}</li></" + blockTag + ">" +
                            "<ul><li>item1</li></ul>" +
                            "<ol><li>item2</li></ol>" +
                            "meta <a href=\"https://www.google.com/search?q&#61;{{ctx.metadata.name}}\" rel=\"nofollow\">Testlink</a> " +
                            "meta</body>"));
        } else {
            assertThat(sanitizedHtml, equalTo(
                    "<head></head><body>" +
                            "<" + blockTag + ">Hello {{ctx.metadata.name}}</" + blockTag + ">" +
                            "<ul><li>item1</li></ul>" +
                            "<ol><li>item2</li></ol>" +
                            "meta <a href=\"https://www.google.com/search?q&#61;{{ctx.metadata.name}}\" rel=\"nofollow\">Testlink</a> " +
                            "meta</body>"));
        }
    }

    public void testDefaultOnClickDisallowed() {
        String badHtml = "<button type=\"button\"" +
                "onclick=\"document.getElementById('demo').innerHTML = Date()\">" +
                "Click me to display Date and Time.</button>";
        HtmlSanitizer sanitizer = new HtmlSanitizer(Settings.EMPTY);
        String sanitizedHtml = sanitizer.sanitize(badHtml);
        assertThat(sanitizedHtml, equalTo("Click me to display Date and Time."));
    }

    public void testDefaultExternalImageDisallowed() {
        String html = "<img src=\"http://test.com/nastyimage.jpg\"/>This is a bad image";
        HtmlSanitizer sanitizer = new HtmlSanitizer(Settings.EMPTY);
        String sanitizedHtml = sanitizer.sanitize(html);
        assertThat(sanitizedHtml, equalTo("This is a bad image"));
    }

    public void testDefault_EmbeddedImageAllowed() {
        String html = "<img src=\"cid:foo\" />This is a good image";
        HtmlSanitizer sanitizer = new HtmlSanitizer(Settings.EMPTY);
        String sanitizedHtml = sanitizer.sanitize(html);
        assertThat(sanitizedHtml, equalTo(html));
    }

    public void testDefaultTablesAllowed() {
        String html = "<table border=\"1\" cellpadding=\"6\">" +
                "<caption>caption</caption>" +
                "<colgroup>" +
                "<col span=\"2\" />" +
                "<col />" +
                "</colgroup>" +
                "<thead>" +
                "<tr>" +
                "<th colspan=\"2\">header1</th>" +
                "<th>header2</th>" +
                "</tr>" +
                "</thead>" +
                "<tfoot>" +
                "<tr>" +
                "<td>Sum</td>" +
                "<td>$180</td>" +
                "</tr>" +
                "</tfoot>" +
                "<tbody>" +
                "<tr>" +
                "<td>cost</td>" +
                "<td>180</td>" +
                "</tr>" +
                "</tbody>" +
                "</table>";
        HtmlSanitizer sanitizer = new HtmlSanitizer(Settings.EMPTY);
        String sanitizedHtml = sanitizer.sanitize(html);
        assertThat(sanitizedHtml, equalTo(html));
    }

    public void testAllowStyles() {
        String html = "<table border=\"1\" cellpadding=\"6\" style=\"color:red\"></table>";
        Settings settings = Settings.builder().putList("xpack.notification.email.html.sanitization.allow", "_tables", "_styles").build();
        HtmlSanitizer sanitizer = new HtmlSanitizer(settings);
        String sanitizedHtml = sanitizer.sanitize(html);
        assertThat(sanitizedHtml, equalTo(html));
    }

    public void testDefaultFormattingAllowed() {
        String html =  "<b></b><i></i><s></s><u></u><o></o><sup></sup><sub></sub><ins></ins><del></del><strong></strong>" +
                "<strike></strike><tt></tt><code></code><big></big><small></small><span></span><br /><em></em><hr />";
        HtmlSanitizer sanitizer = new HtmlSanitizer(Settings.EMPTY);
        String sanitizedHtml = sanitizer.sanitize(html);
        assertThat(sanitizedHtml, equalTo(html));
    }

    public void testDefaultSciptsDisallowed() {
        String html = "<script>doSomethingNefarious()</script>This was a dangerous script";
        HtmlSanitizer sanitizer = new HtmlSanitizer(Settings.EMPTY);
        String sanitizedHtml = sanitizer.sanitize(html);
        assertThat(sanitizedHtml, equalTo("This was a dangerous script"));
    }

    public void testCustomDisabled() {
        String html = "<img src=\"http://test.com/nastyimage.jpg\" />This is a bad image";
        HtmlSanitizer sanitizer = new HtmlSanitizer(Settings.builder()
                .put("xpack.notification.email.html.sanitization.enabled", false)
                .build());
        String sanitizedHtml = sanitizer.sanitize(html);
        assertThat(sanitizedHtml, equalTo(html));
    }

    public void testCustomAllImageAllowed() {
        String html = "<img src=\"http://test.com/nastyimage.jpg\" />This is a bad image";
        HtmlSanitizer sanitizer = new HtmlSanitizer(Settings.builder()
                .put("xpack.notification.email.html.sanitization.allow", "img:all")
                .build());
        String sanitizedHtml = sanitizer.sanitize(html);
        assertThat(sanitizedHtml, equalTo(html));
    }

    public void testCustomTablesDisallowed() {
        String html = "<table><tr><td>cell1</td><td>cell2</td></tr></table>";
        HtmlSanitizer sanitizer = new HtmlSanitizer(Settings.builder()
                .put("xpack.notification.email.html.sanitization.disallow", "_tables")
                .build());
        String sanitizedHtml = sanitizer.sanitize(html);
        assertThat(sanitizedHtml, equalTo("cell1cell2"));
    }

    public void testEnsureSettingsAreRegistered() {
        Settings settings = Settings.builder().put("path.home", createTempDir()).build();
        Watcher watcher = new Watcher(settings);
        for (Setting<?> setting : HtmlSanitizer.getSettings()) {
            assertThat(watcher.getSettings(), hasItem(setting));
        }
    }
}
