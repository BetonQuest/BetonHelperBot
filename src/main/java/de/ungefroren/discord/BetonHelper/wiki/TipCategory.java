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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A category of multiple tips that also contains it's own description
 * <p>
 * Created on 24.09.2018.
 *
 * @author Jonas Blocher
 */
public class TipCategory extends Tip {

    protected final HashMap<String, Tip> subTips;
    protected final List<String> identifiers;

    public TipCategory(String title, String identifier, String text, Iterable<Tip> subTips, String... alternativeIdentifiers) {
        super(title, text, identifier, alternativeIdentifiers);
        this.subTips = new HashMap<>();
        this.identifiers = new ArrayList<>();
        for (Tip subTip : subTips) {
            identifiers.add(subTip.identifier);
            for (String id : subTip.getAllIdentifiers()) {
                this.subTips.put(id.toLowerCase(), subTip);
            }
        }
    }

    /**
     * Gets the sub tip by one of its identifiers (alternate ones will also work)
     *
     * @param identifier an identifier of the sub tip
     * @return the subtip with that identifier
     */
    public final Tip getSubTip(String identifier) {
        return subTips.get(identifier);
    }

    /**
     * Can be used to get the identifiers of all sub tips
     * <p>
     * <b>Note:</b> This list will only contain the main identifiers, not the alternate ones
     *
     * @return a unmodifiable list of all identifiers
     */
    public final List<String> getIdentifiers() {
        return Collections.unmodifiableList(identifiers);
    }
}
