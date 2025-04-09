Helpers are shared python modules that can be used across
multiple streamlit apps. Each helper module  depends upon
streamlit in some fashion.

They are stored in the `helpers` directory and can be imported into any app using the following syntax:

```python
from helpers import my_helper
```

To use helper modules with a Streamlit application, begin by setting up your environment. First, ensure you have Streamlit installed. You can do this by opening your terminal and running the command pip install streamlit. Once Streamlit is installed, add the necessary helper modules to your project by including a helpers directory containing any shared modules you want to use across your Streamlit applications.

Next, import the helper modules into your Streamlit app. To do this, open your Streamlit application file (for example, app.py) and use the syntax from helpers import my_helper to import a specific module from the helpers directory. Replace my_helper with the name of the actual module you want to use. This setup allows you to utilize shared functionalities within your application seamlessly.

Once your imports are set, you’re ready to run the application. Navigate to the directory containing your Streamlit application file in your terminal. To launch the application, enter the command streamlit run Ducky.py, replacing app.py with the actual name of your application file.

If you encounter any import issues, double-check that the helpers directory is correctly placed in the same directory as your app or referenced properly. For detailed usage of each helper module, refer to the module-specific documentation, which outlines functionalities and examples for integration.