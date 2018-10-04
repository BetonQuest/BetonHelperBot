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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import de.ungefroren.discord.BetonHelper.BetonHelperBot;

/**
 * Helper class for working with urls
 * <p>
 * Created on 24.09.2018.
 *
 * @author Jonas Blocher
 */
public class URLHelper {

    /**
     * Method that wraps the creation of {@link URL} and throws RuntimeExceptions instead of {@link MalformedURLException}
     *
     * @param url String containing the url
     * @return the url object
     */
    public static URL create(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method that makes downloading files from a url easier
     *
     * @param url  url to download from
     * @param file where the content from the url should be saved
     * @return if the download was successful
     */
    public static boolean downloadToFile(URL url, File file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            String l;
            while ((l = br.readLine()) != null) {
                bw.write(l);
                bw.newLine();
            }
            br.close();
            bw.close();
            return true;
        } catch (IOException e) {
            BetonHelperBot.log.warn("Could not download " + url.toString() + ": " + e.getMessage());
            return false;
        }
    }
}
