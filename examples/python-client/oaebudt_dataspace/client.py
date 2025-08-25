from functools import wraps
import json
from logging import getLogger
from dataclasses import dataclass, field

import requests


logger = getLogger(__name__)


@dataclass
class DataspaceClient:
    """Client base class which handles authentication via the Tokens API.

    This is mostly just a wrapper around the requests library, which updates
    request headers with a JWT from the Tokens API for authentication.
    """
    domain_name:  str = field(repr=False, default='')
    realm_name:  str = field(repr=False, default='')
    client_id:  str = field(repr=False, default='')
    client_secret:  str = field(repr=False, default='')
    username:  str = field(repr=False, default='')
    password:  str = field(repr=False, default='')

    token: str = ''

    def __post_init__(self):
        """Set tokens Authorization header and add it to client requests."""
        self.header = {}

        self.get = self.authenticated_request(requests.get)
        self.post = self.authenticated_request(requests.post)
        # Can be extended to other methods if needed.

        self.set_token()
        self.set_auth_header()

    def get_token(self):
        """Fetch token from token API endpoint."""
        endpoint = (
            f'https://{self.domain_name}/realms/'
            f'{self.realm_name}/protocol/openid-connect/token'
        )
        data = dict(
            grant_type='password',
            client_id=self.client_id,
            client_secret=self.client_secret,
            username=self.username,
            password=self.password,
        )
        response = requests.post(endpoint, data=data)

        if response.status_code != 200:
            raise ValueError(response.content.decode('utf-8'))

        return response.json()['access_token']

    def set_token(self):
        """Fetch token to be used for requests."""

        token_requirements = (  # Make sure all required fields are set.
            self.domain_name,
            self.realm_name,
            self.client_id,
            self.client_secret,
            self.username,
            self.password,
        )

        if not all(token_requirements):
            raise TypeError(
                "Please set all credentials required to fetch a token."
            )

        self.token = self.get_token()

    def set_auth_header(self, token_has_expired=False):
        """Sets Authorization header for the client using the Bearer schema.

        Args:
            token_has_expired (bool): True if token has expired.
        """
        if not self.token or token_has_expired:
            self.set_token()

        self.header.update(Authorization=f'Bearer {self.token}')

    def authenticated_request(self, func):
        """Decorator to add token authentication to requests."""

        @wraps(func)
        def wrapper(*args, **kwargs):
            kwargs.setdefault('headers', {}).update(self.header)
            response = func(*args, **kwargs)

            if response.status_code in (401, 403):  # Assume token has expired
                self.set_auth_header(token_has_expired=True)
                kwargs['headers'].update(self.header)
                response = func(*args, **kwargs)

            return response
        return wrapper

    def __str__(self):
        return self.__class__.__name__

    # Functionality
    def create_participant_group(
            self,
            group_name,
            participants,  # list
    ):
        endpoint = (
            f'https://{self.domain_name}/api/web/participant/group'
        )
        data = {
            "groupName": group_name,
            "participants": participants,
        }
        return self.post(endpoint, json=data)

    def retrieve_participant_groups(self):
        response = self.get(
            f'https://{self.domain_name}/api/web/participant/group',
        )
        response.raise_for_status()
        return response.json()

    def create_file_based_report(
            self,
            file_path,
            asset_metadata,
            report_title,
            target_audience,    # noqa str: group_name from self.create_participant_group
            report_type='ITEM_REPORT',
    ):
        endpoint = f'https://{self.domain_name}/api/web/report/upload'
        form_data = {  # Form fields dict - Metadata is converted to JSON.
            'title': report_title,
            'reportType': report_type,
            'accessDefinition': f'allow-{target_audience}',
            'metadata': json.dumps(asset_metadata)
        }
        try:
            with open(file_path, 'rb') as f:
                files_payload = {'file': f}

                logger.info("Sending request to:", endpoint)
                logger.info("Form data:", form_data)
                logger.info("File:", file_path)

                response = self.post(
                    endpoint,
                    data=form_data,
                    files=files_payload,
                )
            response.raise_for_status()
            return response

        except FileNotFoundError:
            logger.error(f"Error: The file was not found at {file_path}")
            raise
        except requests.exceptions.RequestException as e:
            logger.error(f"An error occurred with the request: {e}")
            return response
