import streamlit as st

import helpers.sidebar

st.set_page_config(
	page_title="Ducky",
	page_icon="💻",
	layout="wide"
)

helpers.sidebar.show()

st.toast("Welcome to Ducky!", icon="💻")

st.markdown("Welcome to Ducky, your AI-powered personal coding assistant!")
st.write("Ducky is designed to help you explore and understand your code.")

