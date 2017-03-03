"""
Python commands for adding or deleting a Group
"""

import json
import sys
import agate.core


def group_add_arguments(parser):
    """
    Add agate group management arguments
    """

    parser.add_argument('--name', help='The group Name (required), it must be unique', required=True)
    parser.add_argument('--description', help='The group Description', required=False)
    parser.add_argument('--applications',
                        help='Members of a group get access to the applications associated to this group',
                        required=False, nargs='*')


def do_add_command(args):
    """
    Execute add group management command
    """

    try:
        request = agate.core.AgateClient.build(agate.core.AgateClient.LoginInfo.parse(args)).new_request()
        request.fail_on_error()

        group = {'name': args.name}
        if args.description:
            group['description'] = args.description
        if args.applications:
            group['applications'] = args.applications

        request.post().content_type_json().resource(agate.core.UriBuilder(['groups']).build()).content(
            json.dumps(group))

        response = request.send()
        print response.content
    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)


def group_delete_arguments(parser):
    """
    Add agate group management arguments
    """

    parser.add_argument('--name', help='The group Name (required)', required=True)


def do_delete_command(args):
    """
    Execute delete group management command
    """

    try:
        request = agate.core.AgateClient.build(agate.core.AgateClient.LoginInfo.parse(args)).new_request()
        request.fail_on_error()

        request.delete().content_type_json().resource(agate.core.UriBuilder(['group', args.name]).build())

        response = request.send()
        print response.content
    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)
