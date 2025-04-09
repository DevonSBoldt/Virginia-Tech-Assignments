import pytest
from .base_test import BaseTest

@pytest.mark.asyncio
class TestRetrieveNonExistentPrompt(BaseTest):
    async def test_retrieve_non_existent_prompt(self, bob):
        with pytest.raises(Exception, match="Prompt not found"):
            await bob.get_prompt("non-existent-guid")
