import asyncio
import pytest
from dotenv import load_dotenv
import os
from api.client.user import User
from api.session.session import PublicUserSession, PrivateUserSession

load_dotenv()

@pytest.mark.asyncio
class BaseTest:
    TEST_USERNAME = os.getenv('TEST_USERNAME')

    @pytest.fixture(scope='function')
    def alice(self) -> PublicUserSession:
        """Fixture to provide a public session without a user."""
        return PublicUserSession(None, "Alice")

    @pytest.fixture(scope='function')
    def adrianna(self) -> PublicUserSession:
        """Fixture to provide a public session with an admin user."""
        load_dotenv()
        username = os.getenv("ADMIN_TEST_USERNAME")
        password = os.getenv("ADMIN_TEST_PASSWORD")
        user = User(username, password)
        return PublicUserSession(user, "Adrianna")

    @pytest.fixture(scope='function')
    def bob(self) -> PrivateUserSession:
        """Fixture to provide a private session with a user."""
        load_dotenv()
        username = os.getenv("TEST_USERNAME")
        password = os.getenv("TEST_PASSWORD")
        user = User(username, password)
        return PrivateUserSession(user, "Bob")

    @pytest.fixture(autouse=True)
    def clear_prompts_sync(self, adrianna: PublicUserSession, bob: PrivateUserSession):
        yield
        loop = asyncio.get_event_loop()
        loop.run_until_complete(self._clear_prompts(adrianna, bob))

    async def _clear_prompts(self, adrianna: PublicUserSession, bob: PrivateUserSession):
        """Fixture to delete prompts tagged as 'pytest' after every test."""
        try:
            discriminator_tag = "pytest-" + self.TEST_USERNAME
            print(f"Clearing prompts tagged as '{discriminator_tag}'...", flush=True)
            alice_prompts = await adrianna.list_prompts_by_tags([discriminator_tag])
            bob_prompts = await bob.list_prompts_by_tags([discriminator_tag])
            if alice_prompts:
                for prompt in alice_prompts:
                    await adrianna.delete_prompt(prompt['guid'])
            if bob_prompts:
                for prompt in bob_prompts:
                    await bob.delete_prompt(prompt['guid'])
        except Exception as e:
            print(f"Exception in clear_prompts: {e}")

    async def _get_prompt(self, user, guid):
        """Helper method to retrieve a prompt by GUID."""
        return await user.get_prompt(guid)

    async def _assert_prompts_equal(self, client, guid, expected_prompt, actual_prompt):
        """Helper method to compare expected and actual prompt attributes."""
        assert actual_prompt['content'] == expected_prompt['content'], "Prompt content does not match"
        assert actual_prompt['display_name'] == expected_prompt['display_name'], "Prompt display name does not match"
        assert actual_prompt['tags'] == expected_prompt['tags'], "Prompt tags do not match"
