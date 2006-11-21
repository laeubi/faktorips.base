/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.util.localization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Simple class to support developers in internationalisation-process.
 * 
 * All files named "messages.properties" in the source dir (recursively scanned)
 * are compared to all files named "message_ll_RR.properties" (ll means language,
 * e.g. de for german; RR means region, eg. AT for austria; the String ll_RR is given
 * as parameter). If the target file does not exist, it is created with the same content
 * as the source file. If the target exists, the properties contained in the source, 
 * but not in the target, are copied to the target and marked with the text >TRANSLATE_ME<
 * as value-Prefix.
 * <p>
 * Note: No translation is done by this class!
 * 
 * @author Thorsten Guenther
 */
public class LocalizeHelper {
    File sourceRoot;
    File targetRoot;
    String targetLang;
    List modifiedFiles = new ArrayList();
    
    /*
     * Class which supports properties with predictable iteration order.
     */
    private class SortedProperties extends Properties {
        private static final long serialVersionUID = 1L;

        LinkedHashMap content = new LinkedHashMap();
        
        /**
         * {@inheritDoc}
         */
        public String getProperty(String arg0) {
            return (String) content.get(arg0);
        }

        /**
         * {@inheritDoc}
         */
        public synchronized Object setProperty(String arg0, String arg1) {
            return content.put(arg0, arg1);
        }

        /**
         * {@inheritDoc}
         */
        public synchronized void load(InputStream is) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.indexOf("=")>0){
                    String[] props = StringUtils.split(line,"=", 2);
                    if (props.length == 2){
                        content.put(props[0].trim(), props[1].trim());
                    }
                }
            }
        }
        /**
         * {@inheritDoc}
         */
        public void store(OutputStream os, String comments) throws IOException {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "8859_1"));
            if (comments != null){
                writeln(bw, "#" + comments);
            }
            writeln(bw, "#" + new Date().toString());
            for (Iterator iter = content.keySet().iterator(); iter.hasNext();) {
                String key = (String)iter.next();
                bw.write(key);
                bw.write("=");
                bw.write((String)content.get(key));
                bw.newLine();
            }
            bw.flush();
        }
        
        private void writeln(BufferedWriter bw, String s) throws IOException {
            bw.write(s);
            bw.newLine();
        }
        
        /**
         * {@inheritDoc}
         */
        public Set keySet() {
            return content.keySet();
        }
        /**
         * {@inheritDoc}
         */
        public synchronized void clear() {
            content.clear();
        }
        /**
         * {@inheritDoc}
         */
        public synchronized boolean contains(Object arg0) {
            return content.containsValue(arg0);
        }
        /**
         * {@inheritDoc}
         */
        public synchronized boolean containsKey(Object arg0) {
            return content.containsKey(arg0);
        }
        /**
         * {@inheritDoc}
         */
        public boolean containsValue(Object arg0) {
            return content.containsValue(arg0);
        }
        /**
         * {@inheritDoc}
         */
        public Set entrySet() {
            return content.entrySet();
        }
        /**
         * {@inheritDoc}
         */
        public synchronized boolean equals(Object arg0) {
            return content.equals(arg0);
        }
        /**
         * {@inheritDoc}
         */
        public synchronized Object get(Object arg0) {
            return content.get(arg0);
        }
        /**
         * {@inheritDoc}
         */
        public synchronized int hashCode() {
            return content.hashCode();
        }
        /**
         * {@inheritDoc}
         */
        public synchronized boolean isEmpty() {
            return content.isEmpty();
        }
        /**
         * {@inheritDoc}
         */
        public synchronized Object put(Object arg0, Object arg1) {
            return content.put(arg0, arg1);
        }
        /**
         * {@inheritDoc}
         */
        public synchronized void putAll(Map arg0) {
            content.putAll(arg0);
        }
        /**
         * {@inheritDoc}
         */
        public synchronized Object remove(Object arg0) {
            return content.remove(arg0);
        }
        /**
         * {@inheritDoc}
         */
        public synchronized int size() {
            return content.size();
        }
        /**
         * {@inheritDoc}
         */
        public synchronized String toString() {
            return content.toString();
        }
        /**
         * {@inheritDoc}
         */
        public Collection values() {
            return content.values();
        }
        public synchronized Object clone() {
            throw new UnsupportedOperationException();
        }
        protected void rehash() {
            throw new UnsupportedOperationException();
        }        
        public synchronized Enumeration keys() {
            throw new UnsupportedOperationException();
        }
        public synchronized Enumeration elements() {
            throw new UnsupportedOperationException();
        }
        
        public String getProperty(String arg0, String arg1) {
            throw new UnsupportedOperationException();
        }
        public void list(PrintStream arg0) {
            throw new UnsupportedOperationException();
        }
        public void list(PrintWriter arg0) {
            throw new UnsupportedOperationException();
        }
        public Enumeration propertyNames() {
            throw new UnsupportedOperationException();
        }
        public synchronized void save(OutputStream arg0, String arg1) {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * First argument:<br>
     * Path to the base directory, where the source-language files are stored.<br>
     * Second argument:<br>
     * Path to the base directory, where the target-language files are stored.<br>
     * Third argument:<br>
     * The target language (and, optional, the region) as de_RR (e.g. de_AT for 
     * german language with austrian region or only de for german with no special 
     * region information.  
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println(LocalizeHelper.class.getName() + " <source-dir> <target-dir> <target-language>");
            return;
        }

        LocalizeHelper helper = new LocalizeHelper(args[0], args[1], args[2]);
        helper.run();
    }

    /**
     * Creates a new LocalizeHelper woring on the given directories and with the
     * given language.
     * 
     * @param sourceName
     * @param targetName
     * @param targetLang
     */
    public LocalizeHelper(String sourceName, String targetName, String targetLang) {
        sourceRoot = new File(sourceName);
        targetRoot = new File(targetName);
        this.targetLang = "_" + targetLang;
    }

    /**
     * Scan source- and target-dir recursively for files called messages.properites (or, for
     * the target-dir, files with language- and/or region-code appended). Differences are fixed.
     */
    private void run() {
        Hashtable sourceProperties = new Hashtable();
        Hashtable targetProperties = new Hashtable();
        findProperties(sourceRoot, sourceProperties, sourceRoot.getAbsolutePath().length(), "");
        findProperties(targetRoot, targetProperties, targetRoot.getAbsolutePath().length(), targetLang);

        try {
            sync(sourceProperties, targetProperties);
            if (modifiedFiles.size() > 0) {
                System.out.println("Modified files:");
                for (Iterator iter = modifiedFiles.iterator(); iter.hasNext();) {
                    System.out.print("  ");
                    System.out.println(iter.next());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * scan the given directory recursivley for files named messages[_langPostfix].properties. All files
     * found are put into the map <code>propertyFiles</code>, using the slightly modiefied name of the 
     * file as key. The name is prepared for later compere by cutting of the language postfix and file 
     * extension. The first part of the filename of the given length is cut off the name, too. 
     *  
     * @param dir The direcotry to scan. If not a directory, this method returns silently.
     * @param propertyFiles The map to store found files.
     * @param ignorePathPrefixLength The leght of the prefix to be cut off the filename. This is used
     * to cut off the different base-pathnames of source- and target-files.
     * @param langPostfix The language postfix used. Can be the empty string, but not <code>null</code>.
     */
    private void findProperties(File dir, Map propertyFiles, int ignorePathPrefixLength, String langPostfix) {
        if (!dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equalsIgnoreCase("messages" + langPostfix + ".properties")) {
                String name = files[i].getAbsolutePath().substring(ignorePathPrefixLength);

                name = name.substring(0, name.lastIndexOf(langPostfix + ".properties"));
                propertyFiles.put(name, files[i]);
            } else if (files[i].isDirectory()) {
                findProperties(files[i], propertyFiles, ignorePathPrefixLength, langPostfix);
            }
        }
    }

    /**
     * Syncronizes all properties found in the target map with the properties found
     * in the source map.
     * <p>
     * This means to copy the entire file, if not contained in the target map or to 
     * insert all keys missing in the target file.
     * <p>
     * Note: At the moment, no remove of keys contained in target but not in source 
     * is supported.
     *  
     * @param source Map of all found sources. Keys have to be the name of the files
     * without language postfix, file-extension and base-path.
     * @param target Map of all found targets, for keys see sources.
     * @throws IOException If an error occurs during handling the properties files.
     */
    private void sync(Hashtable source, Hashtable target) throws IOException {
        Enumeration sourceNames = source.keys();

        while (sourceNames.hasMoreElements()) {
            String name = (String)sourceNames.nextElement();
            File targetFile = (File)target.get(name);
            if (targetFile == null) {
                File sourceFile = (File)source.get(name);
                String newName = targetRoot.getAbsolutePath() + name + targetLang + ".properties";
                targetFile = new File(newName);
                createFile(targetFile);
                modifiedFiles.add(targetFile);
                FileWriter writer = new FileWriter(targetFile);
                FileReader reader = new FileReader(sourceFile);
                for (int c = reader.read(); c != -1; c = reader.read()) {
                    writer.write(c);
                }
                reader.close();
                writer.close();
            } else {
                SortedProperties sourceProps = new SortedProperties();
                File srcFile = (File)source.get(name);
                sourceProps.load(new FileInputStream(srcFile));

                SortedProperties targetProps = new SortedProperties();
                targetProps.load(new FileInputStream(targetFile));

                Set srcKeys = sourceProps.keySet();
                boolean modified = false;

                for (Iterator iter = srcKeys.iterator(); iter.hasNext();) {
                    String key = (String)iter.next();
                    if (targetProps.getProperty(key) == null) {
                        targetProps.setProperty(key, ">TRANSLATE_ME<" + sourceProps.getProperty(key));
                        modified = true;
                    }
                }
                
                if (modified) {
                    modifiedFiles.add(targetFile);
                    // store the target in same order as the source
                    SortedProperties newTargetProps = new SortedProperties();
                    Set srcKeySet = sourceProps.keySet();
                    for (Iterator iter = srcKeySet.iterator(); iter.hasNext();) {
                        String key = (String)iter.next();
                        String property = targetProps.getProperty(key);
                        newTargetProps.setProperty(key, property);
                    }
                    FileOutputStream out = new FileOutputStream(targetFile);
                    newTargetProps.store(out, "File modified by LocalizationHelper");
                    out.close();
                }
            }
        }
    }

    /**
     * Creates a new file and, if neccessary, the parent directories, too. If the file
     * allready exists, this method returns silently.
     * 
     * @param file The file to create
     * @throws IOException if an error during file creation occurs.
     */
    private void createFile(File file) throws IOException {
        if (file.exists()) {
            return;
        }

        file.getParentFile().mkdirs();
        file.createNewFile();
    }
}
