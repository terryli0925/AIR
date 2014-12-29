from django import template
register = template.Library()

@register.simple_tag
def active(request, pattern):
    import re
    if re.search(pattern, request.path):
        return 'active'
    return ''

# Modified by Compal Electronics, Inc.(2012)
@register.filter
def div(value, arg):
    try:
        value = float(value)
        arg = float(arg)
        if arg: return value/arg
    except : pass
    return ''