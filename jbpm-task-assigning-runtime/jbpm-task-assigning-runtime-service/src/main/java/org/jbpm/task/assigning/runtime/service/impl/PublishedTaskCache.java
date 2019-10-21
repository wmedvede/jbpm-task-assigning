package org.jbpm.task.assigning.runtime.service.impl;

import java.util.HashSet;
import java.util.Set;

/**
 * Convenient repository for keeping track published tasks during runtime. Used for optimization purposes.
 */
public class PublishedTaskCache implements Cloneable {

    private Set<Long> publishedTasks = new HashSet<>();

    public void put(Long taskId) {
        publishedTasks.add(taskId);
    }

    public boolean isPublished(Long taskId) {
        return publishedTasks.contains(taskId);
    }

    public Object clone() {
        try {
            final PublishedTaskCache clone = (PublishedTaskCache) super.clone();
            clone.publishedTasks = new HashSet<>(this.publishedTasks);
            return clone;
        } catch (CloneNotSupportedException e) {
            //will never happen.
            throw new RuntimeException("Failed to clone PublishedTaskCache", e);
        }
    }
}
