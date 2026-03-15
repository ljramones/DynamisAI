package org.dynamisengine.ai.testkit;

import org.dynamisengine.ai.core.GameEngineAdapter;
import org.dynamisengine.ai.core.HeadlessGameEngineAdapter;

class HeadlessGameEngineAdapterContractTest extends GameEngineAdapterContractTest {
    @Override
    protected GameEngineAdapter createAdapter() {
        return new HeadlessGameEngineAdapter();
    }
}
