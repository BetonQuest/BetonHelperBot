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
package de.ungefroren.discord.BetonHelper.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import de.ungefroren.discord.BetonHelper.BetonHelperBot;

/**
 * Helper class for working with files
 *
 * @author Jonas Blocher
 */
public class FileHelper {

    /**
     * Method for easily reading a file from disk into a string
     *
     * @param file the file to read
     * @return content of the file or null if an I/O error occurred
     */
    public static String readToString(File file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            StringBuilder string = new StringBuilder();
            String l;
            while ((l = br.readLine()) != null) string.append(l).append('\n');
            br.close();
            return string.toString();
        } catch (IOException e) {
            BetonHelperBot.log.warn("Could not read " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }
}
