#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

from django.http import HttpResponse, Http404
from django.shortcuts import render, get_object_or_404, redirect
from django.contrib.auth.decorators import login_required

@login_required(login_url='/accounts/login/')
def index(request):
    return render(request, 'build/build_base.html')
