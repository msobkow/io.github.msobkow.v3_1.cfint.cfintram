
// Description: Java 25 in-memory RAM DbIO implementation for MimeType.

/*
 *	io.github.msobkow.CFInt
 *
 *	Copyright (c) 2016-2026 Mark Stephen Sobkow
 *	
 *	Mark's Code Fractal 3.1 CFInt - Internet Essentials
 *	
 *	This file is part of Mark's Code Fractal CFInt.
 *	
 *	Mark's Code Fractal CFInt is available under dual commercial license from
 *	Mark Stephen Sobkow, or under the terms of the GNU Library General Public License,
 *	Version 3 or later.
 *	
 *	Mark's Code Fractal CFInt is free software: you can redistribute it and/or
 *	modify it under the terms of the GNU Library General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *	
 *	Mark's Code Fractal CFInt is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *	
 *	You should have received a copy of the GNU Library General Public License
 *	along with Mark's Code Fractal CFInt.  If not, see <https://www.gnu.org/licenses/>.
 *	
 *	If you wish to modify and use this code without publishing your changes in order to
 *	tie it to proprietary code, please contact Mark Stephen Sobkow
 *	for a commercial license at mark.sobkow@gmail.com
 *	
 */

package io.github.msobkow.v3_1.cfint.cfintram;

import java.math.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import org.apache.commons.codec.binary.Base64;
import io.github.msobkow.v3_1.cflib.*;
import io.github.msobkow.v3_1.cflib.dbutil.*;

import io.github.msobkow.v3_1.cfsec.cfsec.*;
import io.github.msobkow.v3_1.cfint.cfint.*;
import io.github.msobkow.v3_1.cfint.cfintobj.*;
import io.github.msobkow.v3_1.cfsec.cfsecobj.*;
import io.github.msobkow.v3_1.cfint.cfintobj.*;

/*
 *	CFIntRamMimeTypeTable in-memory RAM DbIO implementation
 *	for MimeType.
 */
public class CFIntRamMimeTypeTable
	implements ICFIntMimeTypeTable
{
	private ICFIntSchema schema;
	private Map< CFIntMimeTypePKey,
				CFIntMimeTypeBuff > dictByPKey
		= new HashMap< CFIntMimeTypePKey,
				CFIntMimeTypeBuff >();
	private Map< CFIntMimeTypeByUNameIdxKey,
			CFIntMimeTypeBuff > dictByUNameIdx
		= new HashMap< CFIntMimeTypeByUNameIdxKey,
			CFIntMimeTypeBuff >();

	public CFIntRamMimeTypeTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public void createMimeType( CFSecAuthorization Authorization,
		CFIntMimeTypeBuff Buff )
	{
		final String S_ProcName = "createMimeType";
		CFIntMimeTypePKey pkey = schema.getFactoryMimeType().newPKey();
		pkey.setRequiredMimeTypeId( schema.nextMimeTypeIdGen() );
		Buff.setRequiredMimeTypeId( pkey.getRequiredMimeTypeId() );
		CFIntMimeTypeByUNameIdxKey keyUNameIdx = schema.getFactoryMimeType().newUNameIdxKey();
		keyUNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByUNameIdx.containsKey( keyUNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"MimeTypeUNameIdx",
				keyUNameIdx );
		}

		// Validate foreign keys

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		dictByUNameIdx.put( keyUNameIdx, Buff );

	}

	public CFIntMimeTypeBuff readDerived( CFSecAuthorization Authorization,
		CFIntMimeTypePKey PKey )
	{
		final String S_ProcName = "CFIntRamMimeType.readDerived";
		CFIntMimeTypePKey key = schema.getFactoryMimeType().newPKey();
		key.setRequiredMimeTypeId( PKey.getRequiredMimeTypeId() );
		CFIntMimeTypeBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntMimeTypeBuff lockDerived( CFSecAuthorization Authorization,
		CFIntMimeTypePKey PKey )
	{
		final String S_ProcName = "CFIntRamMimeType.readDerived";
		CFIntMimeTypePKey key = schema.getFactoryMimeType().newPKey();
		key.setRequiredMimeTypeId( PKey.getRequiredMimeTypeId() );
		CFIntMimeTypeBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntMimeTypeBuff[] readAllDerived( CFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamMimeType.readAllDerived";
		CFIntMimeTypeBuff[] retList = new CFIntMimeTypeBuff[ dictByPKey.values().size() ];
		Iterator< CFIntMimeTypeBuff > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	public CFIntMimeTypeBuff readDerivedByUNameIdx( CFSecAuthorization Authorization,
		String Name )
	{
		final String S_ProcName = "CFIntRamMimeType.readDerivedByUNameIdx";
		CFIntMimeTypeByUNameIdxKey key = schema.getFactoryMimeType().newUNameIdxKey();
		key.setRequiredName( Name );

		CFIntMimeTypeBuff buff;
		if( dictByUNameIdx.containsKey( key ) ) {
			buff = dictByUNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntMimeTypeBuff readDerivedByIdIdx( CFSecAuthorization Authorization,
		int MimeTypeId )
	{
		final String S_ProcName = "CFIntRamMimeType.readDerivedByIdIdx() ";
		CFIntMimeTypePKey key = schema.getFactoryMimeType().newPKey();
		key.setRequiredMimeTypeId( MimeTypeId );

		CFIntMimeTypeBuff buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public CFIntMimeTypeBuff readBuff( CFSecAuthorization Authorization,
		CFIntMimeTypePKey PKey )
	{
		final String S_ProcName = "CFIntRamMimeType.readBuff";
		CFIntMimeTypeBuff buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( ! buff.getClassCode().equals( "a103" ) ) ) {
			buff = null;
		}
		return( buff );
	}

	public CFIntMimeTypeBuff lockBuff( CFSecAuthorization Authorization,
		CFIntMimeTypePKey PKey )
	{
		final String S_ProcName = "lockBuff";
		CFIntMimeTypeBuff buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( ! buff.getClassCode().equals( "a103" ) ) ) {
			buff = null;
		}
		return( buff );
	}

	public CFIntMimeTypeBuff[] readAllBuff( CFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamMimeType.readAllBuff";
		CFIntMimeTypeBuff buff;
		ArrayList<CFIntMimeTypeBuff> filteredList = new ArrayList<CFIntMimeTypeBuff>();
		CFIntMimeTypeBuff[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a103" ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new CFIntMimeTypeBuff[0] ) );
	}

	public CFIntMimeTypeBuff readBuffByIdIdx( CFSecAuthorization Authorization,
		int MimeTypeId )
	{
		final String S_ProcName = "CFIntRamMimeType.readBuffByIdIdx() ";
		CFIntMimeTypeBuff buff = readDerivedByIdIdx( Authorization,
			MimeTypeId );
		if( ( buff != null ) && buff.getClassCode().equals( "a103" ) ) {
			return( (CFIntMimeTypeBuff)buff );
		}
		else {
			return( null );
		}
	}

	public CFIntMimeTypeBuff readBuffByUNameIdx( CFSecAuthorization Authorization,
		String Name )
	{
		final String S_ProcName = "CFIntRamMimeType.readBuffByUNameIdx() ";
		CFIntMimeTypeBuff buff = readDerivedByUNameIdx( Authorization,
			Name );
		if( ( buff != null ) && buff.getClassCode().equals( "a103" ) ) {
			return( (CFIntMimeTypeBuff)buff );
		}
		else {
			return( null );
		}
	}

	public void updateMimeType( CFSecAuthorization Authorization,
		CFIntMimeTypeBuff Buff )
	{
		CFIntMimeTypePKey pkey = schema.getFactoryMimeType().newPKey();
		pkey.setRequiredMimeTypeId( Buff.getRequiredMimeTypeId() );
		CFIntMimeTypeBuff existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateMimeType",
				"Existing record not found",
				"MimeType",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateMimeType",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntMimeTypeByUNameIdxKey existingKeyUNameIdx = schema.getFactoryMimeType().newUNameIdxKey();
		existingKeyUNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntMimeTypeByUNameIdxKey newKeyUNameIdx = schema.getFactoryMimeType().newUNameIdxKey();
		newKeyUNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyUNameIdx.equals( newKeyUNameIdx ) ) {
			if( dictByUNameIdx.containsKey( newKeyUNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateMimeType",
					"MimeTypeUNameIdx",
					newKeyUNameIdx );
			}
		}

		// Validate foreign keys

		// Update is valid

		Map< CFIntMimeTypePKey, CFIntMimeTypeBuff > subdict;

		dictByPKey.remove( pkey );
		dictByPKey.put( pkey, Buff );

		dictByUNameIdx.remove( existingKeyUNameIdx );
		dictByUNameIdx.put( newKeyUNameIdx, Buff );

	}

	public void deleteMimeType( CFSecAuthorization Authorization,
		CFIntMimeTypeBuff Buff )
	{
		final String S_ProcName = "CFIntRamMimeTypeTable.deleteMimeType() ";
		String classCode;
		CFIntMimeTypePKey pkey = schema.getFactoryMimeType().newPKey();
		pkey.setRequiredMimeTypeId( Buff.getRequiredMimeTypeId() );
		CFIntMimeTypeBuff existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteMimeType",
				pkey );
		}
		CFIntMimeTypeByUNameIdxKey keyUNameIdx = schema.getFactoryMimeType().newUNameIdxKey();
		keyUNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< CFIntMimeTypePKey, CFIntMimeTypeBuff > subdict;

		dictByPKey.remove( pkey );

		dictByUNameIdx.remove( keyUNameIdx );

	}
	public void deleteMimeTypeByIdIdx( CFSecAuthorization Authorization,
		int argMimeTypeId )
	{
		CFIntMimeTypePKey key = schema.getFactoryMimeType().newPKey();
		key.setRequiredMimeTypeId( argMimeTypeId );
		deleteMimeTypeByIdIdx( Authorization, key );
	}

	public void deleteMimeTypeByIdIdx( CFSecAuthorization Authorization,
		CFIntMimeTypePKey argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		CFIntMimeTypeBuff cur;
		LinkedList<CFIntMimeTypeBuff> matchSet = new LinkedList<CFIntMimeTypeBuff>();
		Iterator<CFIntMimeTypeBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntMimeTypeBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableMimeType().readDerivedByIdIdx( Authorization,
				cur.getRequiredMimeTypeId() );
			deleteMimeType( Authorization, cur );
		}
	}

	public void deleteMimeTypeByUNameIdx( CFSecAuthorization Authorization,
		String argName )
	{
		CFIntMimeTypeByUNameIdxKey key = schema.getFactoryMimeType().newUNameIdxKey();
		key.setRequiredName( argName );
		deleteMimeTypeByUNameIdx( Authorization, key );
	}

	public void deleteMimeTypeByUNameIdx( CFSecAuthorization Authorization,
		CFIntMimeTypeByUNameIdxKey argKey )
	{
		CFIntMimeTypeBuff cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<CFIntMimeTypeBuff> matchSet = new LinkedList<CFIntMimeTypeBuff>();
		Iterator<CFIntMimeTypeBuff> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<CFIntMimeTypeBuff> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableMimeType().readDerivedByIdIdx( Authorization,
				cur.getRequiredMimeTypeId() );
			deleteMimeType( Authorization, cur );
		}
	}
}
