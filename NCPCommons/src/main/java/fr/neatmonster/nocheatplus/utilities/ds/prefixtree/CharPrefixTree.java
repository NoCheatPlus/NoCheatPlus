/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.utilities.ds.prefixtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.CharPrefixTree.CharLookupEntry;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.CharPrefixTree.CharNode;

public class CharPrefixTree<N extends CharNode<N>, L extends CharLookupEntry<N>> extends PrefixTree<Character, N, L>{

    public static class CharNode<N extends CharNode<N>> extends Node<Character, N>{
    }

    public static class SimpleCharNode extends CharNode<SimpleCharNode>{
    }

    public static class CharLookupEntry<N extends CharNode<N>> extends LookupEntry<Character, N>{
        public CharLookupEntry(N node, N insertion, int depth, boolean hasPrefix){
            super(node, insertion, depth, hasPrefix);
        }
    }

    public CharPrefixTree(final NodeFactory<Character, N> nodeFactory, final LookupEntryFactory<Character, N, L> resultFactory) {
        super(nodeFactory, resultFactory);
    }

    /**
     * Auxiliary method to get a List of Character.
     * 
     * @param chars
     * @return
     */
    public static final List<Character> toCharacterList(final char[] chars){
        final List<Character> characters = new ArrayList<Character>(chars.length);
        for (int i = 0; i < chars.length; i++){
            characters.add(chars[i]);
        }
        return characters;
    }

    /**
     * 
     * @param chars
     * @param create
     * @return
     */
    public L lookup(final char[] chars, final boolean create){
        return lookup(toCharacterList(chars), create);
    }

    /**
     * 
     * @param chars
     * @param create
     * @return
     */
    public L lookup(final String input, final boolean create){
        return lookup(input.toCharArray(), create);
    }

    /**
     * 
     * @param chars
     * @return If already inside (not necessarily as former end point).
     */
    public boolean feed(final String input){
        return feed(input.toCharArray());
    }

    /**
     * 
     * @param chars
     * @return If already inside (not necessarily as former end point).
     */
    public boolean feed(final char[] chars){
        return feed(toCharacterList(chars));
    }

    public void feedAll(final Collection<String> inputs, final boolean trim, final boolean lowerCase){
        for (String input : inputs){
            if (trim) input = input.toLowerCase();
            if (lowerCase) input = input.toLowerCase();
            feed(input);
        }
    }

    /**
     * Check if the tree has a prefix of chars. This does not mean a common
     * prefix, but that the tree contains an end point that is a prefix of the
     * input.
     * 
     * @param chars
     * @return
     */
    public boolean hasPrefix(final char[] chars){
        return hasPrefix(toCharacterList(chars));
    }

    /**
     * Check if the tree has a prefix of input. This does not mean a common
     * prefix, but that the tree contains an end point that is a prefix of the
     * input.
     * 
     * @param input
     * @return
     */
    public boolean hasPrefix(final String input){
        return hasPrefix(input.toCharArray());
    }

    /**
     * Quick and dirty addition: Test if a prefix is contained which either
     * matches the whole input or does not end inside of a word in the input,
     * i.e. the inputs next character is a space.
     * 
     * @param input
     * @return
     */
    public boolean hasPrefixWords(final String input) {
        // TODO build this in in a more general way (super classes + stop symbol)!
        final L result = lookup(input, false);
        if (!result.hasPrefix) return false;
        if (input.length() == result.depth) return true;
        if (Character.isWhitespace(input.charAt(result.depth))) return true;
        return false;
    }

    /**
     * Test hasPrefixWords for each given argument.
     * 
     * @param inputs
     * @return true if hasPrefixWords(String) returns true for any of the
     *         inputs, false otherwise.
     */
    public boolean hasAnyPrefixWords(final String... inputs){
        for (int i = 0; i < inputs.length; i++){
            if (hasPrefixWords(inputs[i])){
                return true;
            }
        }
        return false;
    }

    /**
     * Test hasPrefixWords for each element of the collection.
     * 
     * @param inputs
     * @return true if hasPrefixWords(String) returns true for any of the
     *         elements, false otherwise.
     */
    public boolean hasAnyPrefixWords(final Collection<String> inputs){
        for (final String input : inputs){
            if (hasPrefixWords(input)){
                return true;
            }
        }
        return false;
    }

    /**
     * Test if there is an end-point in the tree that is a prefix of any of the
     * inputs.
     * 
     * @param inputs
     * @return
     */
    public boolean hasAnyPrefix(final Collection<String> inputs) {
        for (final String input : inputs) {
            if (hasPrefix(input)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPrefix(final char[] chars){
        return isPrefix(toCharacterList(chars));
    }

    public boolean isPrefix(final String input){
        return isPrefix(input.toCharArray());
    }

    public boolean matches(final char[] chars){
        return matches(toCharacterList(chars));
    }

    public boolean matches(final String input){
        return matches(input.toCharArray());
    }

    /**
     * Factory method for a simple tree.
     * 
     * @param keyType
     * @return
     */
    public static CharPrefixTree<SimpleCharNode, CharLookupEntry<SimpleCharNode>> newCharPrefixTree(){
        return new CharPrefixTree<SimpleCharNode, CharLookupEntry<SimpleCharNode>>(new NodeFactory<Character, SimpleCharNode>(){
            @Override
            public final SimpleCharNode newNode(final SimpleCharNode parent) {
                return new SimpleCharNode();
            }
        }, new LookupEntryFactory<Character, SimpleCharNode, CharLookupEntry<SimpleCharNode>>() {
            @Override
            public final CharLookupEntry<SimpleCharNode> newLookupEntry(final SimpleCharNode node, final SimpleCharNode insertion, final int depth, final boolean hasPrefix) {
                return new CharLookupEntry<SimpleCharNode>(node, insertion, depth, hasPrefix);
            }
        });
    }
}
