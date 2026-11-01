(() => {
	/**
	 * *Since VM2.13.1*
	 *
	 * Appends and returns an element with the specified attributes with the primary purpose of circumventing a strict `Content-Security-Policy` that forbids adding inline code or style.
	 *
	 * @overload
	 * @param
	 * @param {string} tagName - A tag name like `'script'`.
	 * @param {object} attributes - The keys are HTML attributes, not DOM properties, except `textContent` which sets DOM property `textContent`. The values are strings so if you want to assign a private function to `onload` you can do it after the element is created.
	 * @return {void}
	 *
	 * @overload
	 * @param {Node | Element | ShadowRoot} parentNode - The parent node to which the new node will be appended.
	 * It can be inside ShadowDOM: `someElement.shadowRoot`.
	 *
	 * When omitted, it’ll be determined automatically:
	 *
	 * `document.head` (`<head>`) for `script`, `link`, `style`, `meta` tags.
	 * `document.body` (`<body>`) for other tags or when there’s no `<head>`.
	 * `document.documentElement` (`<html>` or an XML root node) otherwise.
	 * @param {string} tagName - A tag name like `'script'`.
	 * @param {object} attributes - The keys are HTML attributes, not DOM properties, except `textContent` which sets DOM property `textContent`. The values are strings so if you want to assign a private function to `onload` you can do it after the element is created.
	 * @return {Element}
	 */
	const addElement = (parentNode, tagName, attributes) => {
		if (typeof parentNode === "string") {
			attributes = tagName;
			tagName = parentNode;

			parentNode =
				tagName === "script" ||
				tagName === "link" ||
				tagName === "style" ||
				tagName === "meta"
					? document.head
					: document.body;
		}

		const el = document.createElement(tagName);

		if (attributes) {
			for (const [key, value] of Object.entries(attributes)) {
				if (key === "textContent") {
					el.textContent = value;
				} else {
					el.setAttribute(key, value);
				}
			}
		}

		parentNode.appendChild(el);

		return el;
	};

	/**
	 * Appends and returns a `<style>` element with the specified CSS.
	 *
	 * @param {string} css - The CSS code to inject.
	 * @returns {HTMLStyleElement}
	 */
	const addStyle = (css) => {
		const el = document.createElement("style");
		el.innerHTML = css;
		document.head.appendChild(el);
		return el;
	};

	/**
	 * Opens URL in a new tab.
	 *
	 * @param {string} url - The URL to open in a new tab. URL relative to current page is also allowed.
	 * @param {{
	 * active: boolean,
	 * container: number,
	 * insert: boolean,
	 * pinned: boolean,
	 * }} [options]
	 * @returns {void}
	 */
	// oxlint-disable-next-line no-unused-vars
	const openInTab = (url, options) => {
		window.open(url);
	};

	const GM = {
		addStyle: async (css) => {
			return addStyle(css);
		},
		/**
		 * *Since VM2.13.1*
		 *
		 * Appends and returns an element with the specified attributes with the primary purpose of circumventing a strict `Content-Security-Policy` that forbids adding inline code or style.
		 *
		 * @overload
		 * @param
		 * @param {string} tagName - A tag name like `'script'`.
		 * @param {object} attributes - The keys are HTML attributes, not DOM properties, except `textContent` which sets DOM property `textContent`. The values are strings so if you want to assign a private function to `onload` you can do it after the element is created.
		 * @return {void}
		 *
		 * @overload
		 * @param {Node | Element | ShadowRoot} parentNode - The parent node to which the new node will be appended.
		 * It can be inside ShadowDOM: `someElement.shadowRoot`.
		 *
		 * When omitted, it’ll be determined automatically:
		 *
		 * `document.head` (`<head>`) for `script`, `link`, `style`, `meta` tags.
		 * `document.body` (`<body>`) for other tags or when there’s no `<head>`.
		 * `document.documentElement` (`<html>` or an XML root node) otherwise.
		 * @param {string} tagName - A tag name like `'script'`.
		 * @param {object} attributes - The keys are HTML attributes, not DOM properties, except `textContent` which sets DOM property `textContent`. The values are strings so if you want to assign a private function to `onload` you can do it after the element is created.
		 * @return {Promise<Element>}
		 */
		addElement: async (parentNode, tagName, attributes) => {
			return addElement(parentNode, tagName, attributes);
		},
		/**
		 * Appends and returns a `<style>` element with the specified CSS.
		 *
		 * @param {string} css - The CSS code to inject.
		 * @returns {Promise<HTMLStyleElement>}
		 */
		/**
		 * Opens URL in a new tab.
		 *
		 * @param {string} url - The URL to open in a new tab. URL relative to current page is also allowed.
		 * @param {{
		 * active: boolean,
		 * container: number,
		 * insert: boolean,
		 * pinned: boolean,
		 * }} [options]
		 * @returns {Promise<void>}
		 */
		// oxlint-disable-next-line no-unused-vars
		openInTab: async (url, options) => {
			openInTab(url, options);
		},
		/**
		 * Makes a request like XMLHttpRequest, with some special capabilities, not restricted by same-origin policy.
		 *
		 * Note: `h` is lowercase (the historical spelling).
		 *
		 * @param {string} url - URL relative to current page is also allowed.
		 * @param {string} [method='GET'] - Usually `GET`.
		 * @param {string} [user] - User for authentication.
		 * @param {string} [password] - Password for authentication.
		 * @param {string} [overrideMimeType] - A MIME type to specify with the request.
		 * @param {Object.<string, string>} [headers] - For example `{ 'name1': 'value1', 'name2': 'value2' }`
		 * - Some special headers are also allowed:
		 * - `'Cookie'`
		 * - `'Host'`
		 * - `'Origin'`
		 * - `'Referer'`
		 * - `'User-Agent'`
		 * @param {'text' | 'json' | 'blob' | 'arraybuffer' | 'document'} [responseType='text'] - One of the following:
		 * - `'text'` -> default
		 * - `'json'`
		 * - `'blob'`
		 * - `'arraybuffer'`
		 * - `'document'` (since VM2.12.0)
		 * @param {number} [timeout] - Time to wait for the request, none by default.
		 * @param {string | ArrayBuffer | Blob | DataView | FormData | ReadableStream | TypedArray | URLSearchParams} [data] - Data to send with the request, usually for `POST` and `PUT` requests.
		 * @param {boolean} [binary] - Send the `data` string as a `blob`. This is for compatibility with Tampermonkey/Greasemonkey, where only `string` type is allowed in `data`.
		 * @param {any} [context] - Can be an object and will be assigned to `context` of the response object.
		 * @param {boolean} [anonymous=false] - When set to `true`, no cookie will be sent with the request and since VM2.12.5 the response cookies will be ignored.
		 * - When absent, an inverted value of Greasemonkey4-compatible `withCredentials` is used. Note that Violentmonkey sends cookies by default, like Tampermonkey, but unlike Greasemonkey4 (same-origin `url` only).
		 * @returns {Promise<void>}
		 */
		xmlhttpRequest: async (
			url,
			method = "GET",
			user,
			password,
			overrideMimeType,
			headers,
			responseType,
			timeout,
			data,
			binary,
			context,
			anonymous = false,
		) => {
			await fetch("turtlebrowse://api/userscript/xmlhttpRequest", {
				headers: headers,
				// oxlint-disable-next-line unicorn/no-invalid-fetch-options
				body: {
					url: url,
					method: method,
					user: user,
					password: password,
					overrideMimeType: overrideMimeType,
					responseType: responseType,
					timeout: timeout,
					data: data,
					binary: binary,
					context: context,
					anonymous: anonymous,
				},
			});
		},
	};

	window.GM = GM;
	window.GM_addElement = addElement;
	window.GM_addStyle = addStyle;
	window.GM_openInTab = openInTab;
})();
