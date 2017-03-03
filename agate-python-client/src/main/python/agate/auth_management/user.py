"""
Python commands for adding or deleting a User
"""

import json
import sys
import agate.core


def user_add_arguments(parser):
    """
    Add agate user management arguments
    """

    parser.add_argument('--name', help='The User Name (required), it must be unique', required=True)
    parser.add_argument('--email', help='The Email (required), it must be unique', required=True)
    parser.add_argument('--upassword', help='The user Password (required)', required=True)
    parser.add_argument('--first-name', help='The user First Name', required=False)
    parser.add_argument('--last-name', help='The user Last Name', required=False)
    parser.add_argument('--applications', help='Applications in which the user can sign in', required=False, nargs='*')
    parser.add_argument('--groups', help='Members of a group get access to the applications associated to this group',
                        required=False, nargs='*')
    parser.add_argument('--role', help='A simple user can only access to its own account (default is: "agate-user")',
                        required=False, default='agate-user')
    parser.add_argument('--status', help='Only active users can sign in (default is "ACTIVE")', required=False,
                        default='ACTIVE')


def do_add_command(args):
    """
    Execute add user management command
    """

    try:
        request = agate.core.AgateClient.build(agate.core.AgateClient.LoginInfo.parse(args)).new_request()
        request.fail_on_error()

        user = {'name': args.name, 'email': args.email, 'role': args.role, 'status': args.status,
                'realm': 'agate-user-realm'}
        if args.first_name:
            user['firstName'] = args.first_name
        if args.last_name:
            user['lastName'] = args.last_name
        if args.applications:
            user['applications'] = args.applications
        if args.groups:
            user['groups'] = args.groups

        data = {'password': args.upassword, 'user': user}

        request.post().content_type_json().resource(agate.core.UriBuilder(['users']).build()).content(json.dumps(data))

        response = request.send()
        print response.content
    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)


def user_delete_arguments(parser):
    """
    Add agate user management arguments
    """

    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument('--name', help='')
    group.add_argument('--email', help='')


def do_delete_command(args):
    """
    Execute delete user management command
    """

    try:
        request = agate.core.AgateClient.build(agate.core.AgateClient.LoginInfo.parse(args)).new_request()
        request.fail_on_error()

        request.get().resource(
            agate.core.UriBuilder(['users', 'find']).query('q', args.name if args.name else args.email).build())

        user_response = request.send()

        new_request = agate.core.AgateClient.build(agate.core.AgateClient.LoginInfo.parse(args)).new_request()
        new_request.fail_on_error()

        new_request.delete().resource(agate.core.UriBuilder(['user', json.loads(user_response.content)['id']]).build())
        response = new_request.send()

        print response.content
    except Exception, e:
        print e
        sys.exit(2)
    except pycurl.error, error:
        errno, errstr = error
        print >> sys.stderr, 'An error occurred: ', errstr
        sys.exit(2)
