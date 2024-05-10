package com.cloudcipher.cloudcipher_client_v2.utility;

class QuickSort
{ 
    private int partition(int p[], int tmp[][], int low, int high) 
    { 
        int[] pivot = tmp[high];  
        int i = (low-1); // index of smaller element 
        for (int j=low; j<high; j++) 
        { 
            // If current element is smaller than or 
            // equal to pivot 
            if (compare(tmp[j], pivot) >= 0) 
            { 
                i++; 
  
                // swap tmp[i] and tmp[j] 
                int[] tempArray = tmp[i]; 
                tmp[i] = tmp[j]; 
                tmp[j] = tempArray; 
                
                // swap p[i] and p[j] 
                int tempInt = p[i]; 
                p[i] = p[j]; 
                p[j] = tempInt; 
            } 
        } 
  
        // swap tmp[i+1] and tmp[high] (or pivot) 
        int[] tempArray = tmp[i+1]; 
        tmp[i+1] = tmp[high]; 
        tmp[high] = tempArray; 
        
        // swap p[i+1] and p[high] 
        int temp = p[i+1]; 
        p[i+1] = p[high]; 
        p[high] = temp; 
  
        return i+1; 
    } 
    
    private int partitionOneArray(int p[], int index[], int low, int high) 
    { 
    	int pivot = p[high];  
        int i = (low-1); // index of smaller element 
        for (int j=low; j<high; j++) 
        { 
            // If current element is smaller than or 
            // equal to pivot 
            if (p[j] >= pivot) 
            { 
                i++; 
  
                // swap tmp[i] and tmp[j] 
                int temp = p[i]; 
                p[i] = p[j]; 
                p[j] = temp; 
                
                // swap p[i] and p[j] 
                temp = index[i]; 
                index[i] = index[j]; 
                index[j] = temp; 
            } 
        } 
  
        // swap tmp[i+1] and tmp[high] (or pivot) 
        int temp = p[i+1]; 
        p[i+1] = p[high]; 
        p[high] = temp; 
        
        // swap p[i+1] and p[high] 
        temp = index[i+1]; 
        index[i+1] = index[high]; 
        index[high] = temp; 
  
        return i+1; 
    } 

    public void sort(int p[], int tmp[][], int low, int high) 
    { 
        if (low < high) 
        { 
            int q = partition(p, tmp, low, high); 
  
            // Recursively sort elements before 
            // partition and after partition 
            sort(p, tmp, low, q-1); 
            sort(p, tmp, q+1, high); 
        } 
    }
    
    public void sortOneArray(int p[], int[] index, int low, int high) 
    { 
        if (low < high) { 
            int q = partitionOneArray(p, index, low, high); 
  
            // Recursively sort elements before 
            // partition and after partition 
            sortOneArray(p, index, low, q-1); 
            sortOneArray(p, index, q+1, high); 
        }
    }
    
    private int compare(int[] a, int[] pivot) {
    	for (int i = 0; i < a.length; i++) {
    		if( a[i] != pivot[i] ) {
    			return (pivot[i] - a[i]);
    		}
    	}
    	return 0;
    }
}