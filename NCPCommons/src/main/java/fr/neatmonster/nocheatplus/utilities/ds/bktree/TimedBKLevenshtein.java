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

import fr.neatmonster.nocheatplus.utilities.ds.bktree.BKModTree.LookupEntry;
import fr.neatmonster.nocheatplus.utilities.ds.bktree.TimedBKLevenshtein.TimedLevenNode;

public class TimedBKLevenshtein<N extends TimedLevenNode<N>, L extends LookupEntry<char[], N>> extends BKLevenshtein<N, L> {

	
	public static class TimedLevenNode<N extends TimedLevenNode<N>> extends LevenNode<N>{
		public long ts;
		/**
		 * Set time to now.
		 * @param value
		 */
		public TimedLevenNode(char[] value) {
			super(value);
			this.ts = System.currentTimeMillis();
		}
		public TimedLevenNode(char[] value, long ts){
			super(value);
			this.ts = ts;
		}
	}
	
	public static class SimpleTimedLevenNode extends TimedLevenNode<SimpleTimedLevenNode>{
		public SimpleTimedLevenNode(char[] value) {
			super(value);
		}
		public SimpleTimedLevenNode(char[] value, long ts){
			super(value, ts);
		}
	}
	
	public TimedBKLevenshtein(NodeFactory<char[], N> nodeFactory, LookupEntryFactory<char[], N, L> resultFactory) {
		super(nodeFactory, resultFactory);
	}

}
