import pytest
from .base_test import BaseTest

@pytest.mark.asyncio
class TestPromptCreationSpecialCharacters(BaseTest):
    
    async def test_create_retrieve_prompt_with_special_characters(self, adrianna):
        expected_prompt = {
            "content": "Special! @#%^&*() content",
            "display_name": "Special # Prompt",
            "tags": ["special_tag$", "unique^tag"]
        }
        expected_guid = await adrianna.add_prompt(expected_prompt)
        actual_prompt = await self._get_prompt(adrianna, expected_guid)
        await self._assert_prompts_equal(adrianna, expected_guid, expected_prompt, actual_prompt)
