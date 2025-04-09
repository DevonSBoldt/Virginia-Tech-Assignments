import pytest
from .base_test import BaseTest

@pytest.mark.asyncio
class TestDuplicatePromptCreation(BaseTest):
    async def test_duplicate_prompt_creation(self, adrianna):
        prompt = {"content": "Duplicate content", "display_name": "Duplicate Test", "tags": ["dup_tag"]}
        guid1 = await adrianna.add_prompt(prompt)
        guid2 = await adrianna.add_prompt(prompt)
        assert guid1 != guid2, "Each prompt should have a unique GUID"
