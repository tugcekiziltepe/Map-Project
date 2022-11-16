import java.util.ArrayList;
import java.util.List;

public class TST<Value> {
    public Node<Value> root;

    // Inserts the key value pair into ternary search tree
    public void put(String key, Value val) {
        //If key length is zero return.
        if (key.length() == 0) return;
        //Call recursive insert method starting from root
        root = insert(root, key, 0, val);

    }

    private Node insert(Node node, String key, int index, Value val) {
        //If node is empty, create new node
        if (node == null) {
            node = new Node<>();
            node.c = key.charAt(index);
            node.val = val;
        }
        //If char at index in key is smaller than node's char, go left node.
        if (key.charAt(index) < node.c) {
            node.left = insert(node.left, key, index, val);
        }
        //If char at index in key is greater than node's char, go right node.
        else if (key.charAt(index) > node.c) {
            node.right = insert(node.right, key, index, val);
        }
        //If char at index in key is equal to node's char, go mid node.
        else {
            //If this node is not last character of key
            if (index + 1 < key.length()) {
                //Increase index by 1.
                node.mid = insert(node.mid, key, index + 1, val);
            }
            //If this node is last character of key
            else {
                node.end = true; //Set end to true
                node.val = val;
            }
        }
        return node;
    }

    // Returns a list of values using the given prefix
    public List<Value> valuesWithPrefix(String prefix) {
        ArrayList<Value> values = new ArrayList<>(); //Create ArrayList to store values that is found.
        return traverseWithPrefix(root, prefix, 0, false, values);
    }

    //Recursive function to find list of values using given prefix
    private ArrayList<Value> traverseWithPrefix(Node<Value> node, String prefix, int index, boolean isFound, ArrayList<Value> values) {
        //If node is not null
        if (node != null) {
            //If prefix is not found yet
            if (!isFound) {
                //If index is smaller than prefix's length
                if (index < prefix.length()) {
                    //If prefix's char at index is smaller than node's char, go left.
                    if (prefix.charAt(index) < node.c) {
                        traverseWithPrefix(node.left, prefix, index, isFound, values);
                    }
                    //If prefix's char at index is equal to node's char, go mid.
                    else if (prefix.charAt(index) == node.c) {
                        //If index + 1 is equal to prefix length, then prefix is found.
                        if (index + 1 == prefix.length()) isFound = true;
                        //Increase index by 1.
                        traverseWithPrefix(node.mid, prefix, index + 1, isFound, values);
                    }
                    //If prefix's char at index is greater than node's char, go right.
                    else if (prefix.charAt(index) > node.c) {
                        traverseWithPrefix(node.right, prefix, index, isFound, values);
                    }
                }

            }
            //If prefix is found
            else {
                //Firstly traverse trie starting from left node.
                traverseWithPrefix(node.left, prefix, index, isFound, values);
                //If node is end of a word, add this value into the list.
                if (node.end) {
                    values.add(node.val);
                }
                //Secondly traverse trie starting mid node.
                traverseWithPrefix(node.mid, prefix, index + 1, isFound, values);
                //Lastly traverse trie starting right node.
                traverseWithPrefix(node.right, prefix, index, isFound, values);
            }
        }
        return values;
    }

    public static class Node<Value> {
        public char c;
        public Node<Value> left, mid, right;
        public Value val;
        boolean end = false;
    }
}