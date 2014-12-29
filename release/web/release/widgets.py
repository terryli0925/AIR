#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

from django import forms
from django.forms.widgets import RadioFieldRenderer, RadioInput
from django.utils.encoding import force_unicode
from django.utils.safestring import mark_safe
from django.utils.html import conditional_escape

class BootstrapInlineRadioInput(RadioInput):
    def render(self, name=None, value=None, attrs=None, choices=()):
        name = name or self.name
        value = value or self.value
        attrs = attrs or self.attrs
        if 'id' in self.attrs:
            label_for = ' for="%s_%s"' % (self.attrs['id'], self.index)
        else:
            label_for = ''
        choice_label = conditional_escape(force_unicode(self.choice_label))
        return mark_safe(u'<label%s class="radio inline">%s %s</label>' % (label_for, self.tag(), choice_label))

    def __unicode__(self):
        return self.render()

class BootstrapInlineRadioRenderer(RadioFieldRenderer):
    def __iter__(self):
        for i, choice in enumerate(self.choices):
            yield BootstrapInlineRadioInput(self.name, self.value, self.attrs.copy(), choice, i)
    
    def __getitem__(self, idx):
        choice = self.choices[idx] # Let the IndexError propogate
        return BootstrapInlineRadioInput(self.name, self.value, self.attrs.copy(), choice, idx)

    def render(self):
        return (mark_safe( u''.join([ u'%s' % force_unicode(w) for w in self ])))
