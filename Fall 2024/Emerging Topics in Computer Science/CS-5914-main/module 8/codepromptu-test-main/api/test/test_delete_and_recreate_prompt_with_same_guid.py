import pytest
from .base_test import BaseTest

@pytest.mark.asyncio
class TestDeleteAndRecreatePrompt(BaseTest):
    
    async def test_delete_and_recreate_prompt_with_same_guid(self, bob):
        initial_prompt = {"content": "Content to delete", "display_name": "Temporary Prompt", "tags": ["temp_tag"]}
        guid = await bob.add_prompt(initial_prompt)
        await bob.delete_prompt(guid)

        new_prompt = {"content": "New content after delete", "display_name": "New Prompt", "tags": ["new_tag"]}
        new_guid = await bob.add_prompt(new_prompt)
        assert guid != new_guid, "The new prompt should have a unique GUID even if similar"
        actual_prompt = await self._get_prompt(bob, new_guid)
        await self._assert_prompts_equal(bob, new_guid, new_prompt, actual_prompt)
