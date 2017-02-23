__author__ = 'Sam'
from django import forms

class LoginForm(forms.Form):
    username = forms.CharField(label='Username', max_length=100,
                               widget=forms.TextInput(attrs={'class':'form-control', 'placeholder': 'Username'}))
    password = forms.CharField(label='Password', max_length=100,
                               widget=forms.PasswordInput(attrs={'class':'form-control', 'placeholder': 'Password'}))
