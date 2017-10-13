def escape_to_xml(string):
    char_to_escaped_string = [
        ('"', '&quot;'),
        ("'", '&apos;'),
        ('<', '&lt;'),
        ('>', '&gt;')
    ]
    for char, escaped in char_to_escaped_string:
        string = string.replace(char, escaped)
    return string


class FilterModule(object):
    def filters(self):
        return {'escape_to_xml': escape_to_xml}
