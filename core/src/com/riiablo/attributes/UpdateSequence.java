package com.riiablo.attributes;

import java.util.Arrays;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import com.riiablo.codec.excel.CharStats;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class UpdateSequence {
  private static final Logger log = LogManager.getLogger(UpdateSequence.class);

  private static final Pool<UpdateSequence> POOL = Pools.get(UpdateSequence.class, 16);
  static UpdateSequence obtain() {
    return POOL.obtain();
  }

  private static final int MAX_SEQUENCE_LENGTH = 32;

  private final StatListGetter[] sequence = new StatListGetter[MAX_SEQUENCE_LENGTH];
  private AttributesUpdater updater;
  private int sequenceLength;
  private boolean sequencing;

  private Attributes attrs;
  private Attributes opBase;
  private CharStats.Entry charStats;

  public Attributes apply() {
    final StatListGetter[] sequence = this.sequence;
    for (int i = 0; i < sequenceLength; i++) {
      final StatListGetter seq = sequence[i];
      updater.add(attrs, seq);
    }

    updater.apply(attrs, charStats, opBase);
    final Attributes attrs = this.attrs;
    clear();
    POOL.free(this);
    return attrs;
  }

  UpdateSequence reset(
      final AttributesUpdater updater,
      final Attributes attrs,
      final int listFlags,
      final Attributes opBase,
      final CharStats.Entry charStats) {
    if (sequencing) {
      throw new IllegalStateException("sequence locked, must apply current sequence");
    }

    this.updater = updater;
    this.attrs = attrs.reset();
    this.opBase = opBase;
    this.charStats = charStats;
    return addAll(attrs, listFlags);
  }

  void clear() {
    sequencing = false;
    Arrays.fill(sequence, 0, sequenceLength, null);
    sequenceLength = 0;
    this.attrs = null;
    this.charStats = null;
    this.updater = null;
  }

  public UpdateSequence add(StatListGetter stats) {
    if (log.traceEnabled()) log.traceEntry("add(stats: {})", stats);
    if (sequenceLength >= MAX_SEQUENCE_LENGTH) {
      throw new IndexOutOfBoundsException(
          "sequenceLength(" + sequenceLength + ") >= MAX_SEQUENCE_LENGTH(" + MAX_SEQUENCE_LENGTH + ")");
    }

    sequence[sequenceLength++] = stats;
    return this;
  }

  public UpdateSequence addAll(Attributes attrs, final int listFlags) {
    if (log.traceEnabled()) log.traceEntry("addAll(attrs: {}, listFlags: {})", attrs, listFlags);
    switch (attrs.type()) {
      case Attributes.AGGREGATE: {
        final int setItemListCount = StatListFlags.countSetItemFlags(listFlags);
        if (setItemListCount > 1) {
          log.warnf("listFlags(0x%x) contains more than 1 set list", listFlags);
        }
        break;
      }
      case Attributes.GEM: {
        final int gemListCount = StatListFlags.countGemFlags(listFlags);
        if (gemListCount == 0) {
          log.warnf("listFlags(0x%x) does not have any gem list selected");
        } else if (gemListCount > 1) {
          log.warnf("listFlags(0x%x) contains more than 1 gem list", listFlags);
        }
        break;
      }
      default: // no-op
        return this;
    }

    final StatList list = attrs.list();
    if (list.isEmpty()) return this;
    for (int i = 0, s = list.numLists(); i < s; i++) {
      if (((listFlags >> i) & 1) == 1) {
        add(list.get(i));
      }
    }

    return this;
  }
}