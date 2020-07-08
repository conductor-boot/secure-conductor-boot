package com.my.conductor.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.my.conductor.conductor.runner.thread.ConductorRunnerThreadProvider;
import com.my.conductor.db.entity.OAuthClientDetails;
import com.my.conductor.dto.custom.principal.CustomPrincipalJsonConverted;
import com.my.conductor.dto.request.client.UserRequest;
import com.my.conductor.dto.request.client.UserResetPasswordRequest;
import com.my.conductor.dto.response.BaseResponseDTO;
import com.my.conductor.service.OAuthUserActionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@SuppressWarnings({ "unchecked", "rawtypes" })
@CrossOrigin(origins = "*")
@RequestMapping("/oauth/user/")
@Tag(name = "OAuth2 User Actions Controller", description = "The API provides the interface for password update to User.")
public class OAuthUserActionsController {
	
	private Logger logger = LoggerFactory.getLogger(OAuthUserActionsController.class.getSimpleName());
	
	@Autowired
	private OAuthUserActionService oAuthUserActionService;
	
	@GetMapping
    public Principal user(Principal principal) {
		
		return principal;
    }
	
	@Operation(summary = "Update existing User i.e. onboard with Client ID and Email address", description = "Takes in the Username , Client ID, Email, Roles. Returns status of the action and error message if any", tags = { "user" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated User and persisted in Database", 
                content = @Content(schema = @Schema(implementation = BaseResponseDTO.class))) ,
        @ApiResponse(responseCode = "400", description = "Invalid Client Id.", content = @Content()),
        @ApiResponse(responseCode = "404", description = "User Not Found.", content = @Content()),
        @ApiResponse(responseCode = "500", description = "Internal Server Error - Returned when an unexpected error occurs on server side", content = @Content())})
	@PutMapping(produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<BaseResponseDTO> updateUser(Principal principal, @RequestBody UserRequest request)
	{
		try {
			
			Gson gson = new Gson();
			
			String customPrincipalJsonStr = gson.toJson(principal);
			
			CustomPrincipalJsonConverted customPrincipal = gson.fromJson(customPrincipalJsonStr, CustomPrincipalJsonConverted.class);  
			
			String client = customPrincipal.getStoredRequest().getClientId();
			
			BaseResponseDTO response = new BaseResponseDTO();
			OAuthClientDetails clientDetails = this.oAuthUserActionService.getClientDetailsByClientId(client);
			
			if(null==clientDetails)
			{
				response.setStatus(false);
				response.setMessage("Client ID Not Found.");
				return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
			}
			else
			{
				String error = this.oAuthUserActionService.updateUser(client, request.getUsername(), request.getEmail(), request.getRoles(), true);
				
				if(null != error)
				{
					response.setStatus(true);
					response.setMessage("User updated successfully");
					return new ResponseEntity(response, HttpStatus.OK);
				}
				else
				{
					response.setStatus(false);
					response.setMessage(error);
					return new ResponseEntity(response, HttpStatus.NOT_FOUND);
				}
			}
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		
		return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@Operation(summary = "Reset existing User's password by username", description = "Taken in the username and new password. Returns a status of the action with a message.", tags = { "user" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully reset password.", 
                content = @Content(schema = @Schema(implementation = BaseResponseDTO.class))) ,
        @ApiResponse(responseCode = "400", description = "Invalid Client Id.", content = @Content()),
        @ApiResponse(responseCode = "404", description = "User Not Found", content = @Content()),
        @ApiResponse(responseCode = "500", description = "Internal Server Error - Returned when an unexpected error occurs on server side", content = @Content())})
	@PatchMapping(produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<BaseResponseDTO> resetUserPassword(Principal principal, @RequestBody UserResetPasswordRequest request)
	{
		try {
			
			BaseResponseDTO response = new BaseResponseDTO();
			
			Gson gson = new Gson();
			
			String customPrincipalJsonStr = gson.toJson(principal);
			
			CustomPrincipalJsonConverted customPrincipal = gson.fromJson(customPrincipalJsonStr, CustomPrincipalJsonConverted.class);  
			
			String client = customPrincipal.getStoredRequest().getClientId();
			
			String error = this.oAuthUserActionService.resetUserPassword(client, request.getUsername(), request.getPassword());
			
			if(null==error)
			{
				response.setStatus(true);
				response.setMessage("User password updated successfully.");
				return new ResponseEntity(response, HttpStatus.OK);
			}
			else if("User NOT found.".equalsIgnoreCase(error))
			{
				response.setStatus(false);
				response.setMessage(error);
				return new ResponseEntity(response, HttpStatus.NOT_FOUND);
			}
			else
			{
				response.setStatus(false);
				response.setMessage(error);
				return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
			
			}
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		
		return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Operation(summary = "Returns the current conductor server status", description = "Returns the current conductor server status", tags = { "conductor" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conductor Server Status Obtained Successfully.", 
                content = @Content(schema = @Schema(implementation = BaseResponseDTO.class))) ,
        @ApiResponse(responseCode = "500", description = "Internal Server Error - Returned when an unexpected error occurs on server side", content = @Content(schema = @Schema(implementation = BaseResponseDTO.class)))})
	@GetMapping(value = "conductor-server-status", produces = "application/json")
	@ResponseBody
	public ResponseEntity<BaseResponseDTO>  getConductorStatus() {
		
		BaseResponseDTO response = new BaseResponseDTO();
		
		try {			
			ConductorRunnerThreadProvider conductorRunnerThread = ConductorRunnerThreadProvider.getInstance();
			
			if(conductorRunnerThread.isRunning())
			{
				response.setStatus(true);
				response.setMessage("Conductor Server is Started");
				return new ResponseEntity(response, HttpStatus.OK);
			
			}
			else
			{
				response.setStatus(true);
				response.setMessage("Conductor Server is Stopped");
				return new ResponseEntity(response, HttpStatus.OK);
			
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.setStatus(false);
			response.setMessage(e.getMessage());
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
