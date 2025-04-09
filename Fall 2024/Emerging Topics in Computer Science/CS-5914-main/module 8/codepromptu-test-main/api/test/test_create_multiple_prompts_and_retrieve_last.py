import pytest
from .base_test import BaseTest

@pytest.mark.asyncio
class TestMultiplePromptCreationRetrieval(BaseTest):
    
    async def test_create_multiple_prompts_and_retrieve_last(self, bob):
        prompt1 = {"content": "First content", "display_name": "First Prompt", "tags": ["first"]}
        prompt2 = {"content": "Second content", "display_name": "Second Prompt", "tags": ["second"]}
        prompt3 = {"content": "Last content", "display_name": "Last Prompt", "tags": ["last"]}
        
        await bob.add_prompt(prompt1)
        await bob.add_prompt(prompt2)
        expected_guid = await bob.add_prompt(prompt3)
        actual_prompt = await self._get_prompt(bob, expected_guid)
        await self._assert_prompts_equal(bob, expected_guid, prompt3, actual_prompt)
