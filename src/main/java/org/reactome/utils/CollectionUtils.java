package org.reactome.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/16/2023
 */
public class CollectionUtils {

    public static <E> List<Collection<E>> split(Collection<E> collection, int numOfCollections) {
        List<Collection<E>> collections = new ArrayList<>();
        double collectionSize = Math.ceil(collection.size() / (double) numOfCollections);

        Iterator<E> iterator = collection.iterator();
        for (int i = 0; i < numOfCollections; i++) {
            Collection<E> splitCollection = new ArrayList<>();

            while (splitCollection.size() < collectionSize && iterator.hasNext()) {
                splitCollection.add(iterator.next());
            }

            if (!splitCollection.isEmpty()) {
                collections.add(splitCollection);
            }
        }
        return collections;
    }
}
