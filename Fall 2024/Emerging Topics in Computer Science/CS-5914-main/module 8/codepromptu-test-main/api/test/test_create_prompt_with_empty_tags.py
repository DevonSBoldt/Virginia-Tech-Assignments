import pytest
from .base_test import BaseTest

@pytest.mark.asyncio
class TestPromptCreationEmptyTags(BaseTest):
    
    async def test_create_prompt_with_empty_tags(self, adrianna):
        expected_prompt = {"content": "No tags content", "display_name": "No Tags Prompt", "tags": []}
        expected_guid = await adrianna.add_prompt(expected_prompt)
        actual_prompt = await self._get_prompt(adrianna, expected_guid)
        assert actual_prompt['tags'] == [], "The tags list should be empty"
        await self._assert_prompts_equal(adrianna, expected_guid, expected_prompt, actual_prompt)
