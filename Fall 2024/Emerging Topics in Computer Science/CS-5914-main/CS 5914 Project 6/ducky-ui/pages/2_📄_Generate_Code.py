import streamlit as st
import asyncio
import pandas as pd
import helpers.sidebar
import helpers.util
import services.prompts as prompts
import services.llm as llm 

# Set up the page configuration
st.set_page_config(
    page_title="Generate Code",
    page_icon="📄",
    layout="wide"
)

# Sidebar (if needed)
helpers.sidebar.show()

# Page Header
st.header("Generate Code")

# Instructions for the user
st.write("Generate and review code using the following features. You can submit your code for review, ask for "
         "modifications, or request debugging assistance.")

# Initialize session state
if "code" not in st.session_state:
    st.session_state.code = ""

if "modification_instruction" not in st.session_state:
    st.session_state.modification_instruction = ""

# Initialize a flag to track button clicks
if "button_active" not in st.session_state:
    st.session_state.button_active = False

# Text area for code input
code = st.text_area(
    label="Paste your code here:",
    value=st.session_state.code,
    height=300
)

# Store the text area code in session state
st.session_state.code = code

# Create columns for buttons
col1, col2, col3, col4 = st.columns(4)

# Helper to run conversation asynchronously without streaming output
# Helper to run conversation asynchronously without streaming output
async def run_llm_conversation(prompt_type, prompt_content):
    if not st.session_state.get("llm_working", False):
        st.session_state.llm_working = True
        st.markdown("### LLM Assistant Working...")

    messages = llm.create_conversation_starter(prompts.system_learning_prompt())
    messages.append({"role": "user", "content": prompt_content})

    full_response = ""
    chunks = llm.converse(messages)

    async for chunk in chunks:
        if chunk == "END OF CHAT":
            break
        if chunk.startswith("EXCEPTION"):
            full_response = ":red[We are having trouble generating advice. Please wait a minute and try again.]"
            break
        full_response += chunk

    st.write(full_response)
    messages.append({"role": "assistant", "content": full_response})

    # Reset the "llm_working" flag after completion
    st.session_state.llm_working = False

    return full_response

# Define the review_prompt function
def review_prompt(code):
    prompt = f"Please review the following code for improvements, best practices, and potential issues:\n\n{code}\n"
    return prompt

# Feature 1: Code Review
if col1.button("Review Code", key="review_button"):
    if code:
        st.session_state.button_active = True  # Set button active flag
        try:
            # Call the newly defined review_prompt function
            review_prompt_content = review_prompt(code)
        except Exception as e:  # Catch any unexpected exceptions
            review_prompt_content = f"Please review the following code:\n\n{code}\n"
            st.error("Warning: An error occurred; using default review prompt.")
        
        asyncio.run(run_llm_conversation("review", review_prompt_content))
        st.session_state.button_active = False  # Reset after completion
    else:
        st.warning("Please provide code for review.")

# Define debug_prompt function inside the Streamlit code
def debug_prompt(code, error_message=None):
    prompt = f"Please help debug the following code:\n\n{code}\n"
    if error_message:
        prompt += f"\nThe following error message was encountered:\n{error_message}\n"
    return prompt

# Use the function in the Debug Code button
if col2.button("Debug Code", key="debug_button"):
    if code:
        # Display the error message input when the "Debug Code" button is pressed
        error_input = st.text_input("Optional: Provide an error message (for debugging purposes):")

        st.session_state.button_active = True  # Set button active flag
        debug_prompt_content = debug_prompt(code, error_input)
        asyncio.run(run_llm_conversation("debug", debug_prompt_content))
        st.session_state.button_active = False  # Reset after completion
    else:
        st.warning("Please provide code to debug.")

# Define the modify_code_prompt function
def modify_code_prompt(code, modification_instruction):
    prompt = f"Please modify the following code as per the instructions:\n\n{code}\n\nInstructions: {modification_instruction}\n"
    return prompt

# Only show modification instructions input when "Modify Code" button is clicked
if col3.button("Modify Code", key="modify_button"):
    if code:
        # Display the modification instructions text area when "Modify Code" is pressed
        modification_instruction = st.text_area(
            "Provide modification instructions below:",
            height=100,
            value=st.session_state.modification_instruction,
            placeholder="Describe how you want to modify the code..."
        )

        st.session_state.button_active = True  # Set button active flag
        modify_prompt = modify_code_prompt(code, modification_instruction)
        asyncio.run(run_llm_conversation("modify", modify_prompt))
        st.session_state.button_active = False  # Reset after completion
    else:
        st.warning("Please provide code for modification.")

# Feature 4: Reset Page
if col4.button("Reset", key="reset_button"):
    # Clear the session state for all inputs
    st.session_state.code = ""  # Clear the code input
    st.session_state.modification_instruction = ""  # Clear modification instructions
    st.session_state.button_active = False  # Reset the button active flag

    # Optionally clear other session states if additional inputs are introduced (like error messages)
    if "error_input" in st.session_state:
        st.session_state.error_input = ""  # Clear error input for debugging

    # Rerun the app to reset the page
    st.rerun()


# Show the code from session state
st.subheader("Current Code in Editor:")
st.code(st.session_state.code, language="python")