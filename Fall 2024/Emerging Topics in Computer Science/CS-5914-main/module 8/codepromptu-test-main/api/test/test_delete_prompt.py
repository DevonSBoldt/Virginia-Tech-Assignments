import pytest
from .base_test import BaseTest

@pytest.mark.asyncio
class TestDeletePrompt(BaseTest):

    async def test_delete_prompt(self, bob):
        # Step 1: Create a prompt
        prompt = {"content": "To be deleted", "display_name": "Deletable Prompt", "tags": ["delete_tag"]}
        guid = await bob.add_prompt(prompt)
        
        # Step 2: Delete the prompt
        await bob.delete_prompt(guid)
        
        # Step 3: Verify that retrieving the prompt raises an error
        with pytest.raises(Exception, match="Prompt not found"):
            await bob.get_prompt(guid)
