package org.qianqianzhu.instrument.util;

public class PackageInfo {

    public static String getNameWithSlash(Class<?> klass){
        return klass.getName().replace('.', '/');
    }

}
