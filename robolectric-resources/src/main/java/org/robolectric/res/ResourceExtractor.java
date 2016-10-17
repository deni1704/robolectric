package org.robolectric.res;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ResourceExtractor implements ResourceIndex {
  private static final Logger LOGGER = Logger.getLogger(ResourceExtractor.class.getName());

  private final String packageName;
  private final Map<ResName, Integer> resourceNameToId = new HashMap<>();
  private final Map<Integer, ResName> resourceIdToResName = new HashMap<>();
  private Integer maxUsedInt = null;

  public ResourceExtractor(ResourcePath resourcePath) {
    packageName = resourcePath.getPackageName();

    if (resourcePath.getRClass() != null) {
      gatherResourceIdsAndNames(resourcePath.getRClass(), packageName);
    }

    if (resourcePath.getInternalRClass() != null) {
      gatherResourceIdsAndNames(resourcePath.getInternalRClass(), packageName);
    }
  }

  private void gatherResourceIdsAndNames(Class<?> rClass, String packageName) {
    for (Class innerClass : rClass.getClasses()) {
      for (Field field : innerClass.getDeclaredFields()) {
        if (field.getType().equals(Integer.TYPE) && Modifier.isStatic(field.getModifiers())) {
          String section = innerClass.getSimpleName();
          int id;
          try {
            id = field.getInt(null);
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }

          if (!section.equals("styleable")) {
            String fieldName = field.getName();
            ResName resName = new ResName(packageName, section, fieldName);

            resourceNameToId.put(resName, id);
            resourceIdToResName.put(id, resName);
          }
        }
      }
    }
  }

  @Override
  public synchronized Integer getResourceId(ResName resName) {
    Integer id = resourceNameToId.get(resName);
    if (id == null && ("android".equals(resName.packageName) || "".equals(resName.packageName))) {
      if (maxUsedInt == null) {
        maxUsedInt = resourceIdToResName.isEmpty() ? 0 : Collections.max(resourceIdToResName.keySet());
      }
      id = ++maxUsedInt;
      resourceNameToId.put(resName, id);
      resourceIdToResName.put(id, resName);
      LOGGER.fine("no id mapping found for " + resName.getFullyQualifiedName() + "; assigning ID #0x" + Integer.toHexString(id));
    }
    return id;
  }

  @Override
  public synchronized ResName getResName(int resourceId) {
    return resourceIdToResName.get(resourceId);
  }

  @Override public Collection<String> getPackages() {
    return Collections.singletonList(packageName);
  }

  @Override public String toString() {
    return "ResourceExtractor{" +
        "package=" + packageName +
        '}';
  }

  @Override
  public Map<ResName, Integer> getAllIdsByResName() {
    return resourceNameToId;
  }

  @Override
  public Map<Integer, ResName> getAllResNamesById() {
    return resourceIdToResName;
  }

  public void addProguardedIds(Set<ResName> resNames) {
    HashSet<ResName> proguardedNames = new HashSet<>(resNames);
    proguardedNames.removeAll(resourceNameToId.keySet());
    System.out.println("proguardedNames = " + proguardedNames);
  }
}