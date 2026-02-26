package org.dynamisai.testkit;

import org.dynamisai.core.GameEngineAdapter;
import org.dynamisai.core.HeadlessGameEngineAdapter;

class HeadlessGameEngineAdapterContractTest extends GameEngineAdapterContractTest {
    @Override
    protected GameEngineAdapter createAdapter() {
        return new HeadlessGameEngineAdapter();
    }
}
