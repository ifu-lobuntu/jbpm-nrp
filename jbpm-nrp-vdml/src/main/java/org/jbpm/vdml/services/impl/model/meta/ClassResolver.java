package org.jbpm.vdml.services.impl.model.meta;


import org.eclipse.emf.common.util.URI;

public class ClassResolver {
    public static Class<?> resolveClass(ClassLoader cl, URI modelUri, String name){
        modelUri=modelUri.trimSegments(1);
        String pkg = modelUri.toPlatformString(true).replace('/','.');
        Class result=null;
        do{
            try {
                pkg=pkg.substring(pkg.indexOf('.',1)+1);
                result=Class.forName(pkg+"." + name,true, cl);
            } catch (ClassNotFoundException e) {
            }
        }while(result==null && pkg.indexOf('.',1)>-1);
        return result;
    }


}
