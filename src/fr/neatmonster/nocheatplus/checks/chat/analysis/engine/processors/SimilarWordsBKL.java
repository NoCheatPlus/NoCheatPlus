package fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors;

import java.util.List;

import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.bktree.SimpleTimedBKLevenshtein;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.bktree.SimpleTimedBKLevenshtein.STBKLResult;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.bktree.TimedBKLevenshtein.SimpleTimedLevenNode;
import fr.neatmonster.nocheatplus.config.ConfigFile;

public class SimilarWordsBKL extends DigestedWords {
	
	public static class SimilarWordsBKLSettings extends DigestedWordsSettings{
		public int maxSize = 1000;
		public int range = 2;
		public long durExpire = 30000;
		public int maxSeek = 0;
		/**
		 * split + compress by default.
		 */
		public SimilarWordsBKLSettings(){
			split = true;
			compress = true;
		}
		public SimilarWordsBKLSettings applyConfig(ConfigFile config, String  prefix){
			super.applyConfig(config, prefix);
			this.maxSize = config.getInt(prefix + "size", this.maxSize);
			this.maxSeek= config.getInt(prefix + "seek", this.maxSeek);
			this.durExpire = (long) (config.getDouble(prefix + "time", (float) this.durExpire / 1000f) * 1000f);
			return this;
		}
	}

	protected final SimpleTimedBKLevenshtein tree = new SimpleTimedBKLevenshtein();
	
	protected int added = 0;
	protected final int maxSize;

	protected final int range;

	protected final long durExpire;
	
	protected final int maxSeek;
	
	protected long lastAdd = System.currentTimeMillis();
	
	public SimilarWordsBKL(String name, SimilarWordsBKLSettings settings){
		super(name, settings);
		this.maxSize = settings.maxSize;
		this.range = settings.range;
		this.durExpire = settings.durExpire;
		this.maxSeek = settings.maxSeek;
	}
	
	@Override
	public void clear() {
		super.clear();
		tree.clear();
	}

	@Override
	public void start(final MessageLetterCount message) {
		if (added + message.words.length > maxSize || System.currentTimeMillis() - lastAdd > durExpire) tree.clear();
		
	}

	@Override
	protected float getScore(final List<Character> chars, final long ts) {
		// TODO: very short words, very long words.
		lastAdd = ts;
		final char[] a = DigestedWords.toArray(chars);
		final STBKLResult result = tree.lookup(a, range, maxSeek, true);
		if (result.isNew) added ++;
		// Calculate time score.
		float score = 0f;
		if (!result.isNew && result.match != null){
			final long age = ts - result.match.ts;
			result.match.ts = ts;
			if (age < durExpire)
				score = Math.max(score, (float) (durExpire - age) / (float) durExpire);
		}
		for (final SimpleTimedLevenNode node : result.nodes){
			final long age = ts - node.ts;
			node.ts = ts;
			if (age < durExpire)
				score = Math.max(score, (float) (durExpire - age) / (float) durExpire);
		}
		return score;
	}

}
