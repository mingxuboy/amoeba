// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name: ConverterArchive.java

package com.meidusa.amoeba.oracle.charset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ConverterArchive {

    public ConverterArchive(){
        m_ifStream = null;
        m_izStream = null;
        m_riStream = null;
        m_rzipFile = null;
    }

    public void openArchiveforInsert(String s) {
        m_izipName = s;
        try {
            m_ifStream = new FileOutputStream(m_izipName);
            m_izStream = new ZipOutputStream(m_ifStream);
        } catch (FileNotFoundException filenotfoundexception) {
        } catch (IOException ioexception) {
        }
    }

    public void closeArchiveforInsert() {
        try {
            m_izStream.close();
            m_ifStream.close();
        } catch (IOException ioexception) {
        }
    }

    public void insertObj(Object obj, String s) {
        ZipEntry zipentry = null;
        Object obj1 = null;
        zipentry = new ZipEntry(s);
        try {
            m_izStream.putNextEntry(zipentry);
            ObjectOutputStream objectoutputstream = new ObjectOutputStream(m_izStream);
            objectoutputstream.writeObject(obj);
            objectoutputstream.close();
            m_izStream.closeEntry();
        } catch (IOException ioexception) {
        }
    }

    public void insertSingleObj(String s, Object obj, String s1) throws IOException {
        Object obj1 = null;
        Object obj2 = null;
        Object obj3 = null;
        Object obj4 = null;
        Object obj5 = null;
        Object obj7 = null;
        Object obj8 = null;
        File file = new File(s);
        if (file.isFile()) {
            try {
                FileInputStream fileinputstream = new FileInputStream(s);
                ZipInputStream zipinputstream = new ZipInputStream(fileinputstream);
                FileOutputStream fileoutputstream = new FileOutputStream("gsstemp.zip");
                ZipOutputStream zipoutputstream = new ZipOutputStream(fileoutputstream);
                int i = zipinputstream.available();
                do {
                    if (zipinputstream.available() == 0) break;
                    ZipEntry zipentry = zipinputstream.getNextEntry();
                    if (zipentry != null && !zipentry.getName().equals(s1)) {
                        zipoutputstream.putNextEntry(zipentry);
                        ObjectInputStream objectinputstream = new ObjectInputStream(zipinputstream);
                        ObjectOutputStream objectoutputstream = new ObjectOutputStream(zipoutputstream);
                        Object obj6 = objectinputstream.readObject();
                        objectoutputstream.writeObject(obj6);
                    }
                } while (true);
                ZipEntry zipentry1 = new ZipEntry(s1);
                zipoutputstream.putNextEntry(zipentry1);
                ObjectOutputStream objectoutputstream1 = new ObjectOutputStream(zipoutputstream);
                objectoutputstream1.writeObject(obj);
                objectoutputstream1.close();
                zipinputstream.close();
            } catch (FileNotFoundException filenotfoundexception) {
                throw new IOException(filenotfoundexception.getMessage());
            } catch (StreamCorruptedException streamcorruptedexception) {
                throw new IOException(streamcorruptedexception.getMessage());
            } catch (IOException ioexception) {
                throw ioexception;
            } catch (ClassNotFoundException classnotfoundexception) {
                throw new IOException(classnotfoundexception.getMessage());
            }
            File file1 = new File("gsstemp.zip");
            file.delete();
            try {
                if (!file1.renameTo(file)) throw new IOException("can't write to target file " + s);
            } catch (SecurityException securityexception) {
                throw new IOException(securityexception.getMessage());
            } catch (NullPointerException nullpointerexception) {
                throw new IOException(nullpointerexception.getMessage());
            }
        } else {
            try {
                FileOutputStream fileoutputstream1 = new FileOutputStream(s);
                ZipOutputStream zipoutputstream1 = new ZipOutputStream(fileoutputstream1);
                ZipEntry zipentry2 = new ZipEntry(s1);
                zipoutputstream1.putNextEntry(zipentry2);
                ObjectOutputStream objectoutputstream2 = new ObjectOutputStream(zipoutputstream1);
                objectoutputstream2.writeObject(obj);
                objectoutputstream2.close();
            } catch (FileNotFoundException filenotfoundexception1) {
                throw new IOException(filenotfoundexception1.getMessage());
            } catch (StreamCorruptedException streamcorruptedexception1) {
                throw new IOException(streamcorruptedexception1.getMessage());
            } catch (IOException ioexception1) {
                throw ioexception1;
            }
        }
        System.out.print(s1 + " has been successfully stored in ");
        System.out.println(s);
    }

    public void insertObjtoFile(String s, String s1, Object obj) throws IOException {
        File file = new File(s);
        File file1 = new File(s + s1);
        if (!file.isDirectory()) throw new IOException("directory " + s + " doesn't exist");
        if (file1.exists()) try {
            file1.delete();
        } catch (SecurityException securityexception) {
            throw new IOException("file exist, can't overwrite file.");
        }
        try {
            FileOutputStream fileoutputstream = new FileOutputStream(file1);
            ObjectOutputStream objectoutputstream = new ObjectOutputStream(fileoutputstream);
            objectoutputstream.writeObject(obj);
            objectoutputstream.close();
        } catch (FileNotFoundException filenotfoundexception) {
            throw new IOException("file can't be created.");
        }
        System.out.print(s1 + " has been successfully stored in ");
        System.out.println(s);
    }

    public void openArchiveforRead() {
        try {
            m_rzipFile = new ZipFile(m_izipName);
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
            System.exit(0);
        }
    }

    public void closeArchiveforRead() {
        try {
            m_rzipFile.close();
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
            System.exit(0);
        }
    }

    public Object readObj(String s) {
        URL url;
        url = getClass().getResource(s);
        if (url == null) return null;

        try {
            Object obj1;
            InputStream inputstream = url.openStream();
            ObjectInputStream objectinputstream = null;
            obj1 = null;
            objectinputstream = new ObjectInputStream(inputstream);
            obj1 = objectinputstream.readObject();
            return obj1;
        } catch (Exception e) {
            return null;
        }

        // Object obj;
        // obj;
        // break MISSING_BLOCK_LABEL_51;
        // obj;
        // return null;
    }

    public Object readObj(String s, String s1) {
        try {
            Object obj3;
            FileInputStream fileinputstream = new FileInputStream(s);
            ZipInputStream zipinputstream = new ZipInputStream(fileinputstream);
            Object obj1 = null;
            Object obj2 = null;
            obj3 = null;
            int i = zipinputstream.available();
            do {
                if (zipinputstream.available() == 0) break;
                ZipEntry zipentry = zipinputstream.getNextEntry();
                if (zipentry == null || !zipentry.getName().equals(s1)) continue;
                ObjectInputStream objectinputstream = new ObjectInputStream(zipinputstream);
                obj3 = objectinputstream.readObject();
                break;
            } while (true);
            zipinputstream.close();
            return obj3;
        } catch (Exception e) {
            return null;
        }

        // Object obj;
        // obj;
        // break MISSING_BLOCK_LABEL_101;
        // obj;
        // return null;
    }

    private String              m_izipName;
    private FileOutputStream    m_ifStream;
    private ZipOutputStream     m_izStream;
    private InputStream         m_riStream;
    private ZipFile             m_rzipFile;
    private static final String TEMPFILE                                    = "gsstemp.zip";
    private static final String _Copyright_2004_Oracle_All_Rights_Reserved_ = null;
    public static final boolean TRACE                                       = false;
    public static final boolean PRIVATE_TRACE                               = false;
    public static final String  BUILD_DATE                                  = "Fri_Sep_29_09:42:23_PDT_2006";

}
