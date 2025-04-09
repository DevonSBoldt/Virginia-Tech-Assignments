import pytest
from .base_test import BaseTest

@pytest.mark.asyncio
class TestPromptRetrievalFailure(BaseTest):
    async def test_prompt_retrieval_failure(self, bob):
        prompt = {"content": "Fail case content", "display_name": "Fail Test", "tags": ["fail_tag"]}
        guid = await bob.add_prompt(prompt)
        # Intentional failure by setting wrong expected GUID
        assert guid == "intentional-failure", "This test is designed to fail"
