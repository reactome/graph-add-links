package org.reactome.graphnodes;

import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.reactome.graphdb.ReactomeGraphDatabase;
import org.reactome.utils.ConfigParser;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.reactome.utils.StringUtils.quote;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/23/2023
 */
public class InstanceEdit extends GraphNode {
    private static InstanceEdit instanceEdit;

    private ZonedDateTime dateTime;
    private String note;
    private String author;

    public static InstanceEdit get() {
        if (instanceEdit == null) {
            instanceEdit = new InstanceEdit("Created by GraphAddLinks", fetchAuthorName());
        }
        return instanceEdit;
    }

    private InstanceEdit(String note, String author) {
        this.note = note;
        this.author = author;
    }

    @Override
    protected String getDisplayName() {
        return this.author + ", " + getDateAsString();
    }

    protected Set<String> getAttributeKeyValueStrings() {
        Set<String> attributeKeyValueStrings = new LinkedHashSet<>();
        attributeKeyValueStrings.addAll(super.getAttributeKeyValueStrings());
        attributeKeyValueStrings.addAll(
            Arrays.asList(
                "dateTime: " + quote(getDateTimeAsString()),
                "note: " + quote(getNote())
            )
        );
        return attributeKeyValueStrings;
    }

    @Override
    public Set<String> getLabels() {
        Set<String> labels = new LinkedHashSet<>();
        labels.addAll(super.getLabels());
        labels.add(getSchemaClass());
        return labels;
    }

    @Override
    public String getSchemaClass() {
        return "InstanceEdit";
    }

    public String getDateAsString() {
        return getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getDateTimeAsString() {
        return getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.S"));
    }

    public ZonedDateTime getDateTime() {
        if (this.dateTime == null) {
            this.dateTime = ZonedDateTime.now();
        }

        return this.dateTime;
    }

    public String getNote() {
        return this.note;
    }

    public String getAuthor() {
        return this.author;
    }

    private static String fetchAuthorName() {
        long personId = Long.parseLong(ConfigParser.getConfigProperty("personId"));
        Value personNode = fetchPersonNode(personId);
        return authorNameFromPersonNode(personNode);
    }

    private static Value fetchPersonNode(long personId) {
        Result result = ReactomeGraphDatabase.getSession().run(
            "MATCH (p:Person) WHERE p.dbId = " + personId + " RETURN p");
        if (result.hasNext()) {
            return result.next().get("p");
        }
        return null;
    }

    private static String authorNameFromPersonNode(Value personNode) {
        final String defaultAuthorName = "Unknown Author";

        if (personNode == null || personNode.isNull()) {
            return defaultAuthorName;
        }

        Value authorNameValue = personNode.get("displayName");
        if (authorNameValue.isNull()) {
            return defaultAuthorName;
        }

        return authorNameValue.asString();
    }
}