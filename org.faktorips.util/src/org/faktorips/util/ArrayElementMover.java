package org.faktorips.util;

/**
 * A helper class that moves a given subset of an array elements one position
 * up or down.
 */
public class ArrayElementMover {

    private Object[] array;
    
    public ArrayElementMover(Object[] array) {
        ArgumentCheck.notNull(array);
        this.array = array;
    }
    
    /**
     * Moves the elements identifies by the given indices one position up
     * in the array this mover is constructed for.  
     * Does not nothing if one of the indexes is 0.
     * 
     * @return the new indexes of the elements that are identified by the given
     * indexes array. 
     */
    public int[] moveUp(int[] indexes) {
        if (contains(indexes, 0)) {
            return indexes;
        }
        int[] newSelection = new int[indexes.length];
        int j=0;
        for (int i=1; i<array.length; i++) {
            if (contains(indexes, i)) {
                swapElements(i-1, i);
                newSelection[j] = i-1;
                j++;
            }
        }
        return newSelection;
    }
    
    /**
     * Moves the elements identifies by the given indices one position down
     * in the array this mover is constructed for.  
     * Does not nothing if one of the indexes is the last index in the array (length-1).
     * 
     * @return the new indexes of the elements that are identified by the given
     * indexes array. 
     */
    public int[] moveDown(int[] indexes) {
        if (contains(indexes, array.length-1)) {
            return indexes;
        }
        int[] newSelection = new int[indexes.length];
        int j=0;
        for (int i=array.length-2; i>=0; i--) {
            if (contains(indexes, i)) {
                swapElements(i, i+1);
                newSelection[j++] = i+1;
            }
        }
        return newSelection;
    }
    
    /*
     * Returns true if the indices array contains the index, otherwise false.
     */
    private boolean contains(int[] indices, int index) {
        for (int i=0; i<indices.length; i++) {
            if (indices[i]==index) {
                return true;
            }
        }
        return false;
    }

    /**
     * Swaps the elements at the given indexes. Can be overridden in subclasses
     * if additional logic is needed.
     */
    protected void swapElements(int index1, int index2) {
        Object temp = array[index1];
        array[index1] = array[index2];
        array[index2] = temp;
    }
    
    
    
    

}
