package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.components.config.value.AlmostBooleanWithOverride;
import fr.neatmonster.nocheatplus.components.config.value.OverrideType;
import fr.neatmonster.nocheatplus.components.config.value.ValueWithOverride;

public class TestConfigValueWithOverride {

    private <T> void expectValueIdentity(ValueWithOverride<T> configValue, T value) {
        if (configValue.getValue() != value) {
            fail("Expect value '" + value + "', got instead: '" + configValue.getValue() + "'.");
        }
    }

    @Test
    public void testAlmostBoolean() {

        AlmostBooleanWithOverride val1 = new AlmostBooleanWithOverride();
        if (!val1.setValue(AlmostBoolean.NO, OverrideType.SPECIFIC)) {
            fail("Override to SPECIFIC after init is expected to work.");
        }
        expectValueIdentity(val1, AlmostBoolean.NO);

        if (!val1.setValue(AlmostBoolean.YES, OverrideType.SPECIFIC)) {
            fail("Override SPECIFIC -> SPECIFIC is expected to work.");
        }
        expectValueIdentity(val1, AlmostBoolean.YES);

    }



}
