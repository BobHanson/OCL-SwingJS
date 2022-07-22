package com.actelion.research.chem.descriptor.flexophore;

import com.actelion.research.util.ArrayUtils;

import java.util.Arrays;

public class MolDistHistHelper {

    /**
     * Creates a single {@link MolDistHist} object from several MolDistHist. Missing descriptor histograms are added
     * and filled with ones.
     * @param arr
     * @return
     */
    public static MolDistHist assemble (MolDistHist ... arr){

        int nNodesSum = 0;
        int maxNumNodes = 0;
        for (MolDistHist mdhFrag : arr) {
            int nNodes = mdhFrag.getNumPPNodes();
            nNodesSum += nNodes;
            if(nNodes>maxNumNodes)
                maxNumNodes=nNodes;
        }
        MolDistHist mdh = new MolDistHist(nNodesSum);
        int [] [] arrMapIndexNew = new int[arr.length][maxNumNodes];
        int indexNew = 0;
        for (int i = 0; i < arr.length; i++) {
            int n = arr[i].getNumPPNodes();
            for (int j = 0; j < n; j++) {
                arrMapIndexNew[i][j]=indexNew;
                indexNew++;
                mdh.addNode(arr[i].getNode(j));
            }
        }

        for (int i = 0; i < arr.length; i++) {
            int n = arr[i].getNumPPNodes();
            MolDistHist mdhFrag = arr[i];

            for (int j = 0; j < n; j++) {
                for (int k = j+1; k < n; k++) {
                    byte [] arrDistHist = mdhFrag.getDistHist(j,k);
                    int index1 = arrMapIndexNew[i][j];
                    int index2 = arrMapIndexNew[i][k];
                    mdh.setDistHist(index1, index2, arrDistHist);
                }
            }
        }

        // Fill the missing distance histograms
        for (int i = 0; i < nNodesSum; i++) {
            for (int j = i+1; j < nNodesSum; j++) {
                byte [] arrDistHist = mdh.getDistHist(i,j);
                if(isZero(arrDistHist)){
                    Arrays.fill(arrDistHist, (byte)1);
                    mdh.setDistHist(i, j, arrDistHist);
                }
                // System.out.println(ArrayUtils.toString(arrDistHist));
            }
        }

        return mdh;
    }
    public static boolean isZero(byte [] b){
        boolean z=true;
        for (byte v : b) {
            if(v!=0){
                z=false;
                break;
            }
        }
        return z;
    }

    public static String toStringDistHist(MolDistHist mdh){
        StringBuilder sb = new StringBuilder();
        int nNodes = mdh.getNumPPNodes();
        for (int i = 0; i < nNodes; i++) {
            for (int j = i+1; j < nNodes; j++) {
                byte [] arrDistHist = mdh.getDistHist(i,j);

                sb.append(ArrayUtils.toString(arrDistHist));
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static void setDistHistToOne(MolDistHist mdh){
        int nNodes = mdh.getNumPPNodes();
        for (int i = 0; i < nNodes; i++) {
            for (int j = i+1; j < nNodes; j++) {
                byte [] arrDistHist = mdh.getDistHist(i,j);
                Arrays.fill(arrDistHist, (byte) 1);
                mdh.setDistHist(i,j, arrDistHist);
            }
        }
    }


}