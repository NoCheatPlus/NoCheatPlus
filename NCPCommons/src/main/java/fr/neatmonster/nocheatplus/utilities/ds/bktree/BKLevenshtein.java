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
package fr.neatmonster.nocheatplus.utilities.ds.bktree;

import fr.neatmonster.nocheatplus.utilities.ds.bktree.BKLevenshtein.LevenNode;
import fr.neatmonster.nocheatplus.utilities.ds.bktree.BKModTree.LookupEntry;

/**
 * Some version of a BK-Levenshtein tree.
 * @author mc_dev
 *
 * @param <N>
 */
public class BKLevenshtein<N extends LevenNode<N>, L extends LookupEntry<char[], N>> extends BKModTree<char[], N, L> {
	
	/**
	 * Fat default impl.
	 * @author mc_dev
	 *
	 * @param <N>
	 */
	public static class LevenNode<N extends LevenNode<N>> extends HashMapNode<char[], N>{
		public LevenNode(char[] value) {
			super(value);
		}
	}
	
	public BKLevenshtein(NodeFactory<char[], N> nodeFactory, LookupEntryFactory<char[], N, L> resultFactory) {
		super(nodeFactory, resultFactory);
	}

//	@Override
//	public int distance(final char[] s1, final char[] s2) {
//		/*
//		 * Levenshtein distance, also known as edit distance.
//		 * Not optimal implementation (m*n space).
//		 * Gusfield, Dan (1999), Algorithms on Strings, Sequences and Trees. Cambridge: University Press. 
//		 * (2012/09/07) http://en.literateprograms.org/Levenshtein_distance_%28Java%29#chunk%20use:usage 
//		 */
//		final int n = s1.length;
//		final int m = s2.length;
//		if (n == m){
//			// Bad style "fix", to return 0 on equality.
//			int match = 0;
//			for (int i = 0; i < n; i++){
//				if (s1[i] == s2[i]) match ++;
//				else break;
//			}
//			if (match == m) return 0;
//		}
//		final int[][] dp = new int[n + 1][m + 1];
//		for (int i = 0; i < dp.length; i++) {
//			for (int j = 0; j < dp[i].length; j++) {
//				dp[i][j] = i == 0 ? j : j == 0 ? i : 0;
//				if (i > 0 && j > 0) {
//					if (s1[i - 1] == s2[j - 1])
//						dp[i][j] = dp[i - 1][j - 1];
//					else
//						dp[i][j] = Math.min(dp[i][j - 1] + 1, Math.min(
//								dp[i - 1][j - 1] + 1, dp[i - 1][j] + 1));
//				}
//			}
//		}
//		return dp[n][m];
//	}

	@Override
	public int distance(char[] s, char[] t) {
		/*
		 * Adapted from CheckUtils to char[].
         * NOTE: RETURNS 1 FOR SAME STRINGS.
		 */
		// if (s == null || t == null)
		// throw new IllegalArgumentException("Strings must not be null");

		int n = s.length;
		int m = t.length;
		
		if (n == m){
			// Return equality faster.
			for (int i = 0; i < n; i++){
				if (s[i] == t[i]) m --;
				else break;
			}
			if (m == 0) return 0;
			m = n; // Reset.
		}

		if (n == 0)
			return m;
		else if (m == 0)
			return n;

		if (n > m) {
			final char[] tmp = s;
			s = t;
			t = tmp;
			n = m;
			m = t.length;
		}

		int p[] = new int[n + 1];
		int d[] = new int[n + 1];
		int _d[];

		int i;
		int j;

		char t_j;

		int cost;

		for (i = 0; i <= n; i++)
			p[i] = i;

		for (j = 1; j <= m; j++) {
			t_j = t[j - 1];
			d[0] = j;

			for (i = 1; i <= n; i++) {
				cost = s[i - 1] == t_j ? 0 : 1;
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1]
						+ cost);
			}

			_d = p;
			p = d;
			d = _d;
		}

		return p[n];
	}

}
