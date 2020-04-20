/*******************************************************************************
 * (c) Copyright IBM Corporation 2017.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package demo.war;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = "/session-updater")
public class SessionUpdaterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SESSION_PARAM_FEELING = "feeling";
	private static final String POST_PARAM_SAVE = "save";

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter writer = response.getWriter();
		HttpSession session = request.getSession();

		response.setContentType("text/html");

		if (null != request.getParameter("invalidate")) {
			session.invalidate();
			response.sendRedirect("shello.jsp");
			return; // <--- Here.
		}

		String toSave = request.getParameter(POST_PARAM_SAVE);
		if (null != toSave) {
			writer.append("Thanks for telling me you feel '" + toSave + "'");
			session.setAttribute(SESSION_PARAM_FEELING, toSave);
		} else {
			writer.append("You didn't tell me how you feel.");
		}
		writer.append("<p><a href=\"shello.jsp\">Go back</a></p>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}
