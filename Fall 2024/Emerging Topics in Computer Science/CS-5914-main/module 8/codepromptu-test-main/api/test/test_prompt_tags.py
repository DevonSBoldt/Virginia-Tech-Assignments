import pytest
from .base_test import BaseTest

@pytest.mark.asyncio
class TestPromptTags(BaseTest):
    async def test_prompt_tags(self, adrianna):
        prompt = {"content": "Testing tags", "display_name": "Tag Test", "tags": ["unique_tag"]}
        guid = await adrianna.add_prompt(prompt)
        retrieved_prompt = await self._get_prompt(adrianna, guid)
        assert "unique_tag" in retrieved_prompt['tags'], "Prompt should contain 'unique_tag'"
