/**
 * BetonHelperBot
 * Copyright (C) 2018 Jonas Blocher
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.ungefroren.discord.BetonHelper.wiki;

import java.io.File;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ungefroren.discord.BetonHelper.BetonHelperBot;
import de.ungefroren.discord.BetonHelper.utils.FileHelper;
import de.ungefroren.discord.BetonHelper.utils.URLHelper;

/**
 * Contains all knowledge data for the helper bot and synchronizes it from the wiki
 * <p>
 * Created on 24.09.2018.
 *
 * @author Jonas Blocher
 */
public class BetonWiki {

    private static final URL
            REFERENCE_RAW_URL = URLHelper.create("https://raw.githubusercontent.com/wiki/Co0sh/BetonQuest/Reference.md"),
            EVENTS_LIST_RAW_URL = URLHelper.create("https://raw.githubusercontent.com/wiki/Co0sh/BetonQuest/Events-List.md"),
            CONDITIONS_LIST_RAW_URL = URLHelper.create("https://raw.githubusercontent.com/wiki/Co0sh/BetonQuest/Conditions-List.md"),
            OBJECTIVES_LIST_RAW_URL = URLHelper.create("https://raw.githubusercontent.com/wiki/Co0sh/BetonQuest/Objectives-List.md"),
            VARIABLES_LIST_RAW_URL = URLHelper.create("https://raw.githubusercontent.com/wiki/Co0sh/BetonQuest/Variables-List.md");

    private static final String
            EVENTS_LIST_BASE_URL = "https://github.com/Co0sh/BetonQuest/wiki/Events-List",
            CONDITIONS_LIST_BASE_URL = "https://github.com/Co0sh/BetonQuest/wiki/Conditions-List",
            OBJECTIVES_LIST_BASE_URL = "https://github.com/Co0sh/BetonQuest/wiki/Objectives-List",
            VARIABLES_LIST_BASE_URL = "https://github.com/Co0sh/BetonQuest/wiki/Variables-List";

    private static final File
            REFERENCE_FILE = new File("Reference.md"),
            EVENTS_LIST_FILE = new File("Events-List.md"),
            CONDITIONS_LIST_FILE = new File("Conditions-List.md"),
            OBJECTIVES_LIST_FILE = new File("Objectives-List.md"),
            VARIABLES_LIST_FILE = new File("Variables-List.md");

    private static final Pattern
            //Match a events description, group 1 is the title, group 2 the identifier, group 3 could be modifiers
            // (static/peristent) and group 4 is the description
            EVENTS_REGEX = Pattern.compile(
            "(?m)^[ \\t]*##[ \\t]+([^:\\n]+)[ \\t]*:[ \\t]*`([^`\\n]+)`[ \\t]*(_[^\\n]+_)?[ \\t\\n]*((\\n?[ \\t]*[^\\n#]*)+)$"),
    //Match a conditions description, group 1 is the title, group 2 the identifier, group 3 could be modifiers
    // (static/peristent) and group 4 is the description
    CONDITIONS_REGEX = Pattern.compile(
            "(?m)^[ \\t]*##[ \\t]+([^:\\n]+)[ \\t]*:[ \\t]*`([^`\\n]+)`[ \\t]*(_[^\\n]+_)?[ \\t\\n]*((\\n?[ \\t]*[^\\n#]*)+)$"),
    //Match a objectives description, group 1 is the title, group 2 the identifier, group 3 could be modifiers
    // (static/peristent) and group 4 is the description
    OBJECTIVES_REGEX = Pattern.compile(
            "(?m)^[ \\t]*##[ \\t]+([^:\\n]+)[ \\t]*:[ \\t]*`([^`\\n]+)`[ \\t]*(_[^\\n]+_)?[ \\t\\n]*((\\n?[ \\t]*[^\\n#]*)+)$"),
    //Match a variables description, group 1 is the title, group 2 the identifier and group 3 is the description
    VARIABLES_REGEX = Pattern.compile(
            "(?m)^[ \\t]*##[ \\t]+([^:\\n]+)[ \\t]*:[ \\t]*`([^`\\n]+)`[ \\t\\n]*((\\n?[ \\t]*[^\\n#]*)+)$");


    private final Tip help = new Tip("Need help with the bot?",
                                     "This bot is able to show you the descriptions of all events, conditions objectives and" +
                                             " variables.\n" +
                                             "To get some help just mention the bot in a message and also write the category and the" +
                                             " name of what you are searching for.\n" +
                                             "For example: `@BetonHelper event message`\n" +
                                             "You can also just write a category to list all objects (`@BetonHelper events` will " +
                                             "list all events).\n" +
                                             "Instead of mentioning the bot you can also write him private messages to not spam any " +
                                             "textchannels if you are working privately.\n" +
                                             "**Now have fun!** :blush:",
                                     "help");
    private HashMap<String, Tip> tips;
    private ZonedDateTime synchronized_timestamp = null;

    /**
     * Gets the url to a section of the wiki
     *
     * @param baseUrl      the base url of the site that contains the section
     * @param sectionTitle the title of the section
     * @return the direct url to the section
     */
    private static String getSectionUrl(String baseUrl, String sectionTitle) {
        return baseUrl + "#" + sectionTitle.toLowerCase().replaceAll("[^a-zA-Z\\s0-9]", "").replaceAll("\\s", "-");
    }

    private static String getSectionUrl(String baseUrl, String... sectionTitle) {
        StringBuilder titleBuilder = new StringBuilder();
        for (String string : sectionTitle) {
            if (string == null) continue;
            if (titleBuilder.length() != 0) titleBuilder.append(' ');
            titleBuilder.append(string);
        }
        return getSectionUrl(baseUrl, titleBuilder.toString());
    }

    /**
     * Synchronizes the bots knowledge database with the wiki
     */
    public void synchronizeWiki() {
        BetonHelperBot.log.info("Starting synchronization of wiki...");
        boolean success = true;
        if (!URLHelper.downloadToFile(EVENTS_LIST_RAW_URL, EVENTS_LIST_FILE)) success = false;
        if (!URLHelper.downloadToFile(CONDITIONS_LIST_RAW_URL, CONDITIONS_LIST_FILE)) success = false;
        if (!URLHelper.downloadToFile(OBJECTIVES_LIST_RAW_URL, OBJECTIVES_LIST_FILE)) success = false;
        if (!URLHelper.downloadToFile(VARIABLES_LIST_RAW_URL, VARIABLES_LIST_FILE)) success = false;
        if (success) {
            synchronized_timestamp = ZonedDateTime.now();
        } else {
            BetonHelperBot.log.warn("Not all data could be downloaded from the wiki. Bot uses local files instead...");
        }
        tips = new HashMap<>();
        addTip(help);

        {//Load events
            String eventsContent = FileHelper.readToString(EVENTS_LIST_FILE);
            if (eventsContent == null) {
                BetonHelperBot.log.error("Couldn't read " + EVENTS_LIST_FILE.getName());
                System.exit(1);
                return;
            }
            Matcher eventsMatcher = EVENTS_REGEX.matcher(eventsContent);
            List<Tip> tips = new ArrayList<>();
            while (eventsMatcher.find()) {
                final String
                        title = eventsMatcher.group(1),
                        identifier = eventsMatcher.group(2),
                        attributes = eventsMatcher.group(3),
                        text = eventsMatcher.group(4),
                        url = getSectionUrl(EVENTS_LIST_BASE_URL, title, identifier, attributes);
                Tip tip = new Tip(title + " event", text, identifier)
                        .addAdditionalInformation("Attributes:", attributes)
                        .addAdditionalInformation("Link:", url);
                tips.add(tip);
            }
            Tip events = new TipCategory("Events List", "event", null, tips, "events") {
                @Override
                public String getText() {
                    String txt = "Here is a list of all known events:\n";
                    StringJoiner join = new StringJoiner(", ");
                    for (String id : this.identifiers) join.add(id);
                    txt += "```" + join.toString() + "```";
                    return txt;
                }
            }.addAdditionalInformation("Link:", EVENTS_LIST_BASE_URL);
            addTip(events);
        }

        {//Load conditions
            String conditionsContent = FileHelper.readToString(CONDITIONS_LIST_FILE);
            if (conditionsContent == null) {
                BetonHelperBot.log.error("Couldn't read " + CONDITIONS_LIST_FILE.getName());
                System.exit(1);
                return;
            }
            Matcher conditionsMatcher = CONDITIONS_REGEX.matcher(conditionsContent);
            List<Tip> tips = new ArrayList<>();
            while (conditionsMatcher.find()) {
                final String
                        title = conditionsMatcher.group(1),
                        identifier = conditionsMatcher.group(2),
                        attributes = conditionsMatcher.group(3),
                        text = conditionsMatcher.group(4),
                        url = getSectionUrl(CONDITIONS_LIST_BASE_URL, title, identifier, attributes);
                Tip tip = new Tip(title + " condition", text, identifier)
                        .addAdditionalInformation("Attributes:", attributes)
                        .addAdditionalInformation("Link:", url);
                tips.add(tip);
            }
            Tip conditions = new TipCategory("Conditions List", "condition", null, tips, "conditions") {
                @Override
                public String getText() {
                    String txt = "Here is a list of all known conditions:\n";
                    StringJoiner join = new StringJoiner(", ");
                    for (String id : this.identifiers) join.add(id);
                    txt += "```" + join.toString() + "```";
                    return txt;
                }
            }.addAdditionalInformation("Link:", CONDITIONS_LIST_BASE_URL);
            addTip(conditions);
        }

        {//Load objectives
            String objectivesContent = FileHelper.readToString(OBJECTIVES_LIST_FILE);
            if (objectivesContent == null) {
                BetonHelperBot.log.error("Couldn't read " + OBJECTIVES_LIST_FILE.getName());
                System.exit(1);
                return;
            }
            Matcher objectivesMatcher = OBJECTIVES_REGEX.matcher(objectivesContent);
            List<Tip> tips = new ArrayList<>();
            while (objectivesMatcher.find()) {
                final String
                        title = objectivesMatcher.group(1),
                        identifier = objectivesMatcher.group(2),
                        attributes = objectivesMatcher.group(3),
                        text = objectivesMatcher.group(4),
                        url = getSectionUrl(OBJECTIVES_LIST_BASE_URL, title, identifier, attributes);
                Tip tip = new Tip(title + " objective", text, identifier)
                        .addAdditionalInformation("Attributes:", attributes)
                        .addAdditionalInformation("Link:", url);
                tips.add(tip);
            }
            Tip objectives = new TipCategory("Objectives List", "objective", null, tips, "objectives") {
                @Override
                public String getText() {
                    String txt = "Here is a list of all known objectives:\n";
                    StringJoiner join = new StringJoiner(", ");
                    for (String id : this.identifiers) join.add(id);
                    txt += "```" + join.toString() + "```";
                    return txt;
                }
            }.addAdditionalInformation("Link:", OBJECTIVES_LIST_BASE_URL);
            addTip(objectives);
        }

        {//Load variables
            String variablesContent = FileHelper.readToString(VARIABLES_LIST_FILE);
            if (variablesContent == null) {
                BetonHelperBot.log.error("Couldn't read " + VARIABLES_LIST_FILE.getName());
                System.exit(1);
                return;
            }
            Matcher variablesMatcher = VARIABLES_REGEX.matcher(variablesContent);
            List<Tip> tips = new ArrayList<>();
            while (variablesMatcher.find()) {
                final String
                        title = variablesMatcher.group(1),
                        identifier = variablesMatcher.group(2),
                        text = variablesMatcher.group(3),
                        url = getSectionUrl(VARIABLES_LIST_BASE_URL, title, identifier);
                Tip tip = new Tip(title + " variable", text, identifier)
                        .addAdditionalInformation("Link:", url);
                tips.add(tip);
            }
            Tip variables = new TipCategory("Variables List", "variable", null, tips, "variables") {
                @Override
                public String getText() {
                    String txt = "Here is a list of all known variables:\n";
                    StringJoiner join = new StringJoiner(", ");
                    for (String id : this.identifiers) join.add(id);
                    txt += "```" + join.toString() + "```";
                    return txt;
                }
            }.addAdditionalInformation("Link:", VARIABLES_LIST_BASE_URL);
            addTip(variables);
        }
        BetonHelperBot.log.info("Update of database successful!");
    }

    /**
     * Gets a tip that should be sent as answer to a message
     *
     * @param message the message to answer
     * @return the fitting tip from the knowledge database
     */
    public Tip findTip(String message) {
        String[] words = message.split("[^\\w]+");
        outer:
        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            if (word.isEmpty() || word.startsWith(BetonHelperBot.getInstance().getSelfUserName().toLowerCase()))
                continue outer;
            Tip tip = tips.get(word);
            if (tip != null) {
                if (tip instanceof TipCategory) {
                    TipCategory category = (TipCategory) tip;
                    inner:
                    for (int j = 0; j < words.length; j++) {
                        if (j == i)
                            continue inner;
                        word = words[j].toLowerCase();
                        if (word.isEmpty() || word.startsWith(BetonHelperBot.getInstance().getSelfUserName().toLowerCase()))
                            continue inner;
                        Tip subTip = category.getSubTip(word);
                        if (subTip != null) return subTip;
                    }
                    return category;
                } else {
                    return tip;
                }
            }
        }
        return help;
    }

    /**
     * Adds a tip with all its alternate identifiers to the list
     *
     * @param tip
     */
    private void addTip(Tip tip) {
        for (String identifier : tip.getAllIdentifiers()) {
            tips.put(identifier.toLowerCase(), tip);
        }
    }

    /**
     * @return the timestamp when the wiki was last synchronized
     */
    public ZonedDateTime getSynchronized_timestamp() {
        return synchronized_timestamp;
    }
}
