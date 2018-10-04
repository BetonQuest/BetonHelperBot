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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


/**
 * A tip that the bot can send you if you need help, containing all information about a topic.
 * <p>
 * Each can be identified by one or multiple keywords (called identifiers).
 * <p>
 * Created on 24.09.2018.
 *
 * @author Jonas Blocher
 */
public class Tip {

    protected final String identifier;
    protected final String[] alternativeIdentifiers;
    protected final String text;
    protected final String title;
    protected final List<AdditionalInfo> additionalInformation;

    public Tip(String title, String text, String identifier, String... alternativeIdentifiers) {
        this.title = title;
        this.text = text;
        this.identifier = identifier;
        this.alternativeIdentifiers = alternativeIdentifiers;
        this.additionalInformation = new ArrayList<>();
    }

    /**
     * @return the main identifier of this tip
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * @return a set of all identifiers, including the main one and all alternatives
     */
    public final Set<String> getAllIdentifiers() {
        Set<String> all = new HashSet<>(Arrays.asList(alternativeIdentifiers));
        all.add(identifier);
        return all;
    }

    /**
     * @return the full description supplied by this tip
     */
    public String getText() {
        return text;
    }

    /**
     * @return the title of this tip
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return any additional information added to this tip
     */
    public List<AdditionalInfo> getAdditionalInformation() {
        return Collections.unmodifiableList(additionalInformation);
    }

    /**
     * Adds additional information to the tip. If either title or content are null or empty the information won't be added.
     * @param title the title of the information
     * @param content the content
     * @return instance of this tip for qu
     */
    public Tip addAdditionalInformation(String title, String content) {
        if (title == null || content == null || title.isEmpty() || content.isEmpty()) return this;
        additionalInformation.add(new AdditionalInfo(title, content));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tip tip = (Tip) o;
        return Objects.equals(identifier, tip.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    public static class AdditionalInfo {

        private final String title;
        private final String content;

        public AdditionalInfo(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }
    }
}
